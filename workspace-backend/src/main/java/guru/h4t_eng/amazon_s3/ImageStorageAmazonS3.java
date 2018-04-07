package guru.h4t_eng.amazon_s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.util.ContentTypeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * ImageStorageAmazonS3.
 *
 * Created by aalexeenka on 11/10/2015.
 */
public class ImageStorageAmazonS3 {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(ImageStorageAmazonS3.class);

    private static final String BUCKET_NAME;
    private static final String IMG_START_URL;
    private static final String ACCESS_KEY;
    private static final String SECRET_KEY;

    public static final String S3_PREFIX;
    public static final String CLOUD_FRONT_PREFIX;
    private static final String S3_ENV;

    private static final int MAX_WIDTH = 600;
    private static final int MAX_HEIGHT = 400;
    private static final String SUFFIX = "/";

    private static final ImageStorageAmazonS3 instance;

    private final AmazonS3Client s3client;

    static
    {
        Properties prop = new Properties();
        InputStream in = ImageStorageAmazonS3.class.getClassLoader().getResourceAsStream("amazon-s3.properties");
        if (in == null) {
            throw new RuntimeException("Can't find file");
        }
        try {
            prop.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Can't load data from file");
        }
        IOUtils.closeQuietly(in);

        BUCKET_NAME = prop.getProperty("bucket-name");
        IMG_START_URL = prop.getProperty("img-start-url");
        ACCESS_KEY = prop.getProperty("access-key");
        SECRET_KEY = prop.getProperty("secret-key");
        S3_PREFIX = prop.getProperty("s3-prefix");
        CLOUD_FRONT_PREFIX = prop.getProperty("cloud-front-prefix");
        S3_ENV = prop.getProperty("s3-env");

    }


    private ImageStorageAmazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        final ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        s3client = new AmazonS3Client(credentials, clientConfiguration);

        if ("dev".equals(S3_ENV)) {
            s3client.setS3ClientOptions(S3ClientOptions.builder()
                    .setPathStyleAccess(true)
                    .disableChunkedEncoding()
                    .build());
            s3client.setEndpoint(S3_PREFIX);
        } else {
            s3client.setRegion(Region.EU_Frankfurt.toAWSRegion());
        }
    }

    static
    {
        try
        {
            instance = new ImageStorageAmazonS3();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Can't create instance of ImageStorageAmazonS3.", e);
        }
    }

    public static ImageStorageAmazonS3 getInstance()
    {
        return instance;
    }


    // Run only fo test evn!
    public String uploadImg(String imgURL, String fileFolder, String fileName) throws IOException {
        long startDate = new Date().getTime();
        // 1. download img
        final URLConnection urlConnection = openConnection(imgURL);
        final InputStream urlStream = urlConnection.getInputStream();

        final String contentType = urlConnection.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Can't get Content Type for URL: " + imgURL);
        }

        final String ext = ContentTypeUtil.getExt(contentType);
        if (ext == null) {
            throw new RuntimeException("Can't define ext for contentType: " + contentType);
        }

        BufferedImage img = ImageIO.read(urlStream);
        urlStream.close();

        long imgDownloadDate = new Date().getTime();

        // 2. resize if needed
        if (img == null) {
            throw new RuntimeException("Can't get img for URL: " + imgURL);
        }
        long resizingDate = -1;
        if (img.getWidth() > MAX_WIDTH || img.getHeight() > MAX_HEIGHT) {
            int width = -1, height = -1;
            if (img.getWidth() > MAX_WIDTH) {
                width = MAX_WIDTH;
                height = (int) (((double) img.getHeight()) * ((double)width / img.getWidth()));
            } else if (img.getHeight() > MAX_HEIGHT) {
                height = MAX_HEIGHT;
                width = (int) (((double) img.getWidth()) * ((double)height / img.getHeight()));
            }
            img = scale(img, width, height);
            resizingDate = new Date().getTime();
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, ext, os);
        final byte[] buf = os.toByteArray();
        ByteArrayInputStream imgStream = new ByteArrayInputStream(buf);
        if (buf.length == 0) {
            throw new RuntimeException("Can't get img for url: " + imgURL + ", content_type: " + contentType);
        }
        long beforeUploadDate = new Date().getTime();

        // 3. upload to Amazon
        String result = uploadToAmazonS3(imgStream, buf.length, contentType, ext, fileFolder, fileName);
        long finishDate = new Date().getTime();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Total time: " + (finishDate - startDate)
                    + ". Download image time: " + (imgDownloadDate - startDate)
                    + (resizingDate != -1 ? ". Resize Img Time: " + (resizingDate - imgDownloadDate) : "")
                    + ". Upload Time to Amazon: " + (finishDate - beforeUploadDate));
        }

        return result;
    }

    public BufferedImage scale(BufferedImage img, int targetWidth, int targetHeight) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;

        int w = img.getWidth();
        int h = img.getHeight();

        int prevW = w;
        int prevH = h;

        do {
            if (w > targetWidth) {
                w /= 2;
                w = (w < targetWidth) ? targetWidth : w;
            }

            if (h > targetHeight) {
                h /= 2;
                h = (h < targetHeight) ? targetHeight : h;
            }

            if (scratchImage == null) {
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);

            prevW = w;
            prevH = h;
            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return ret;
    }

    public void deleteImg(UUID userId, String engVal) {
        java.util.List<String> imgURLs = WordDataSource.getInstance().loadImgURL(userId, engVal);
        if (CollectionUtils.isEmpty(imgURLs)) {
            return;
        }

        for (String imgURL: imgURLs) {
            if (StringUtils.isBlank(imgURL)) {
                continue;
            }

            if (!imgURL.startsWith(IMG_START_URL)) {
                continue;
            }

            s3client.deleteObject(BUCKET_NAME, imgURL.substring(imgURL.indexOf(userId.toString())));
        }
    }


    public String uploadToAmazonS3(
            InputStream imgStream,
            String contentType,
            String fileFolder,
            String engVal
    ) throws IOException {
        final String ext = ContentTypeUtil.getExt(contentType);
        if (ext == null) {
            throw new RuntimeException("Can't define ext for contentType: " + contentType);
        }
        return uploadToAmazonS3(imgStream, -1, contentType, ext, fileFolder, engVal);
    }

    public String uploadToAmazonS3(
            InputStream stream,
            int contentLength,
            String contentType,
            String ext,
            String fileFolder,
            String fileName
    ) throws IOException {
        String finalPath = fileFolder + SUFFIX + fileName + "." + ext;
        return uploadToAmazonS3(stream, contentLength, contentType, finalPath);
    }

    public String uploadToAmazonS3(
            InputStream stream,
            int contentLength,
            String contentType,
            String s3Path
    ) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        if (contentLength != -1) metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        final PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Path, stream, metadata).withCannedAcl(CannedAccessControlList.PublicRead);
        s3client.putObject(putObjectRequest);

        return S3_PREFIX + BUCKET_NAME + "/" + s3Path; // maybe replace that logic in future
        // return s3client.getResourceUrl(BUCKET_NAME, s3Path);
        // upload file to folder and set it to public
//        final PutObjectResult putObjectResult = s3client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, new File("e:\\cat.jpg"))
//                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public static class HeaderOption {
        public HeaderOption(String name, String value) {
            this.name = name;
            this.value = value;
        }

        // public static HeaderOption CROSS_ORIGIN_ALL = new HeaderOption("Access-Control-Allow-Origin", "*");

        private String name;
        private String value;
    }

    public void uploadFile(File file, String s3Path, HeaderOption... headerOptions) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setCacheControl("max-age=31536000");

        for (HeaderOption headerOption : headerOptions) {
            metadata.setHeader(headerOption.name, headerOption.value);
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Path, file);
        putObjectRequest.setMetadata(metadata);

        s3client.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public boolean isExist(String s3Path) {
        boolean result;
        try (S3Object s3Object = ImageStorageAmazonS3.getInstance().s3client.getObject(ImageStorageAmazonS3.BUCKET_NAME, s3Path)) {
            result = s3Object != null;
        } catch (IOException e) {
            LOG.error("isExist {}", s3Path, e);
            throw new RuntimeException("s3Path=" + s3Path, e);
        } catch (AmazonS3Exception e) {
            return false;
        }

        return result;
    }


    private URLConnection openConnection(String url) throws IOException {
        URLConnection hc = new URL(url).openConnection();
        hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
        //hc.setRequestProperty("Referer", "https://www.google.by/");
        hc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //hc.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        hc.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6");
        hc.setRequestProperty("Cache-Control", "no-cache");
        hc.setRequestProperty("Connection", "keep-alive");
        hc.setDoOutput(true);
        hc.connect();
        return hc;
    }
}