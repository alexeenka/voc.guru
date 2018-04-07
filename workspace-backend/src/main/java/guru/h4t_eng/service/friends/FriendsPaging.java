package guru.h4t_eng.service.friends;

import com.google.gson.annotations.SerializedName;
import guru.h4t_eng.config.ApplicationProperties;

/**
 *
 * Paging class, it uses for <b>VK</b> or <b>FACEBOOK</b> integration. <br>
 * In case of VK, all data set manually. <br>
 * <br>
 * In case of Facebook, data set after parsing json: <br>
 * Example:
 * <pre>
 * "paging": {
 *   "cursors": {
 *     "before": "MTMxNzgyOTYwNjMzMTM5",
 *     "after": "MTA1NzU1ODIzMjQyNTY4"
 *   },
 *   "next": "https://graph.facebook.com/v2.8/YXBwbGljYXRpb25fY29udGV4dDoxOTQyNjg2NTc1OTg4NzQZD/friends_using_app?access_token=EAACwr7JTwZAoBAD1TiIZAo8H4ZBP5U5dEoAjtPrkPCJ7eSeAaAGZCwRv2zdkqcyFatt3uCZC2FLOZBkM2nLyGme9OERSR38jy0hIQorXSnZCu7ioEY6j4lGWIZBfeHDMxga0hrQmkegPzTZC3UXUVBdN63sEp4v6iLIoHyvJ77oKoUAZDZD&pretty=1&limit=5&after=MTA1NzU1ODIzMjQyNTY4"
 *  }
 * </pre>
 *  Created by aalexeenka on 21.11.2016.
 */
public class FriendsPaging {

    private String next;

    @SerializedName("previous")
    private String prev;

    private int page;

    private int pageSize = ApplicationProperties.getInstance().getFriendPagingSize();

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
