'use strict';

module.exports = function(grunt) {
    require('time-grunt')(grunt);
    require('jit-grunt')(grunt);

    grunt.loadNpmTasks('grunt-properties-reader');

    var shelljs = require('shelljs');
    var currentPath = shelljs.pwd();
    var workspaceProductionPath = shelljs + "../../workspace-production/";

    grunt
        .initConfig({
            app: {
                source: '../src/main/frontend',
                dist: '../target/h4t-eng.guru',
                awsS3_dist: '../target/awsS3',
                workspace_production_path: '../../workspace-production'
            },
            // don't need clean command, because it's maven job
            copy: {
                // for DEV using full version of libs: angular, bootstrap, etc without uglify
                debug_vendor_js: {
                    options: {
                        flatten: true
                    },
                    files: [
                        { src: 'bower_components/angular/angular.js', dest: '<%= app.dist %>/js/vendor/angular-1.5.0.js' },
                        { src: 'bower_components/angular-animate/angular-animate.js', dest: '<%= app.dist %>/js/vendor/angular-animate-1.5.0.js' },
                        { src: 'bower_components/angular-cookies/angular-cookies.js', dest: '<%= app.dist %>/js/vendor/angular-cookies-1.5.0.js' },
                        { src: 'bower_components/angular-bootstrap/ui-bootstrap.js', dest: '<%= app.dist %>/js/vendor/ui-bootstrap-0.14.3.js' },
                        { src: 'bower_components/angular-bootstrap/ui-bootstrap-tpls.js', dest: '<%= app.dist %>/js/vendor/ui-bootstrap-tpls-0.14.3.js' },
                        { src: 'bower_components/angular-ui-router/release/angular-ui-router.js', dest: '<%= app.dist %>/js/vendor/angular-ui-router-0.2.15.js' },
                        { src: 'outside-bower/modernizr/modernizr.min.2.8.3.js', dest: '<%= app.dist %>/js/vendor/modernizr-2.8.3.js' },
                        { src: 'bower_components/jquery/dist/jquery.js', dest: '<%= app.dist %>/js/vendor/jquery-2.2.1.js' },
                        { src: 'bower_components/jquery-ui/jquery-ui.js', dest: '<%= app.dist %>/js/vendor/jquery-ui-1.11.4.js'},
                        { src: 'bower_components/moment/moment.js', dest: '<%= app.dist %>/js/vendor/moment-2.10.6.js' }
                    ]
                },
                awsS3_fonts: {
                    options: {
                        flatten: true
                    },
                    files: [
                        // bootstrap fonts
                        { src: 'bower_components/bootstrap/dist/fonts/glyphicons-halflings-regular.ttf', dest: '<%= app.awsS3_dist %>/fonts/glyphicons-halflings-regular.ttf' },
                        { src: 'bower_components/bootstrap/dist/fonts/glyphicons-halflings-regular.woff', dest: '<%= app.awsS3_dist %>/fonts/glyphicons-halflings-regular.woff' },
                        { src: 'bower_components/bootstrap/dist/fonts/glyphicons-halflings-regular.woff2', dest: '<%= app.awsS3_dist %>/fonts/glyphicons-halflings-regular.woff2' },
                        // font-awesome
                        { src: 'bower_components/components-font-awesome/fonts/FontAwesome.otf', dest: '<%= app.awsS3_dist %>/fonts/FontAwesome.otf' },
                        { src: 'bower_components/components-font-awesome/fonts/fontawesome-webfont.eot', dest: '<%= app.awsS3_dist %>/fonts/fontawesome-webfont.eot' },
                        { src: 'bower_components/components-font-awesome/fonts/fontawesome-webfont.svg', dest: '<%= app.awsS3_dist %>/fonts/fontawesome-webfont.svg' },
                        { src: 'bower_components/components-font-awesome/fonts/fontawesome-webfont.ttf', dest: '<%= app.awsS3_dist %>/fonts/fontawesome-webfont.ttf' },
                        { src: 'bower_components/components-font-awesome/fonts/fontawesome-webfont.woff', dest: '<%= app.awsS3_dist %>/fonts/fontawesome-webfont.woff' },
                        { src: 'bower_components/components-font-awesome/fonts/fontawesome-webfont.woff2', dest: '<%= app.awsS3_dist %>/fonts/fontawesome-webfont.woff2' },
                    ]
                },
                static_html: {
                    options: {flatten: true},
                    files: [
                        { src: '<%= app.source %>/index.html', dest: '<%= indexHtml%>'},
                        { src: '<%= app.source %>/login.html', dest: '<%= loginHtml %>'}
                    ]
                },
                common_properties_files: {
                    files: [
                        {expand: true, cwd: '../+resources/common', src: '**/*.properties', dest: '<%= app.dist %>/WEB-INF/classes/'}
                    ]
                },
                dev_properties_files: {
                    files: [
                        {expand: true, cwd: '../+resources/dev', src: '**/*.properties', dest: '<%= app.dist %>/WEB-INF/classes/'}
                    ]
                },
                prod_properties_files: {
                    files: [
                        {expand: true, cwd: '../+resources/prod', src: '**/*.properties', dest: '<%= app.dist %>/WEB-INF/classes/'}
                    ]
                },
                dev_js_files: {
                    files: [
                        { src: '<%= app.source %>/main.js', dest: '<%= app.dist %>/js/app/main.js'},
                        {expand: true, cwd: '<%= app.source %>/html', src: '**/*.js', dest: '<%= app.dist %>/js/app'}
                    ]
                },
                css_file : {
                    options: {flatten: true},
                    files: [
                        { src: '<%= app.source %>/css/h4t-eng.app.css', dest: '<%= app.dist %>/css/app/<%=app_css%>'},
                        { src: '<%= app.source %>/css/h4t-eng.login.app.css', dest: '<%= app.dist %>/css/app/<%=app_login_css%>'}
                    ]
                }
            },
            // guide: http://ng-learn.org/2014/08/Populating_template_cache_with_html2js/
            // plugin: https://github.com/karlgoldstein/grunt-html2js
            html2js: {
                options: {
                    base: 'not_work',
                    module: 'h4t-eng.templates',
                    rename: function (moduleName) {
                        return moduleName.replace('../../src/main/frontend', '');
                    },
                    singleModule: true,
                    useStrict: true,
                    htmlmin: {
                        collapseBooleanAttributes: true,
                        collapseWhitespace: true,
                        removeAttributeQuotes: true,
                        removeComments: true,
                        removeEmptyAttributes: true,
                        removeRedundantAttributes: true,
                        removeScriptTypeAttributes: true,
                        removeStyleLinkTypeAttributes: true
                    }
                },
                main: {
                    src: ['<%= app.source %>/html/**/*.html'],
                    dest: '<%= app.dist %>/js/app/populate_template_cache.js'
                }
            },
            // https://github.com/gruntjs/grunt-contrib-uglify
            uglify: {
                awsS3_vendor_js: {
                    options: {
                        compress: {},
                        mangle: false,
                        preserveComments: false,
                        report: 'min'
                    },
                    files: {
                        '<%= app.awsS3_dist %>/js/vendor_0.9.js': [
                            'bower_components/angular/angular.min.js',
                            'bower_components/angular-animate/angular-animate.min.js',
                            'bower_components/angular-cookies/angular-cookies.min.js',
                            'bower_components/jquery/dist/jquery.min.js',
                            'bower_components/jquery-ui/jquery-ui.min.js',
                            'bower_components/bootstrap/dist/js/bootstrap.min.js',
                            'bower_components/angular-bootstrap/ui-bootstrap.min.js',
                            'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
                            'bower_components/angular-ui-router/release/angular-ui-router.min.js',
                            'outside-bower/modernizr/modernizr.min.2.8.3.js',
                            'bower_components/moment/min/moment.min.js',
                            'bower_components/ua-parser-js/dist/ua-parser.min.js',
                            'bower_components/jquery.easing/js/jquery.easing.min.js',
                            'bower_components/lodash/dist/lodash.min.js'
                        ]
                    }
                },
                awsS3_app_js: {
                    options: {
                        compress: {},
                        mangle: false,
                        preserveComments: false,
                        report: 'min'
                    },
                    files: {
                        '<%= app.awsS3_dist %>/js/app/h4t-eng.app-<%=buildVersion%>.js': [
                            '<%= app.source %>/main.js',
                            '<%= app.source %>/html/common/global.js',
                            '<%= app.dist %>/js/app/populate_template_cache.js',

                            '<%= app.source %>/html/common/knowledge-service.js',
                            '<%= app.source %>/html/common/common-service.js',
                            '<%= app.source %>/html/common/counter-service.js',
                            '<%= app.source %>/html/create-knowledge/create-knowledge.js',
                            '<%= app.source %>/html/create-knowledge/create-word/create-word.js',
                            '<%= app.source %>/html/speech-settings/speech-settings.js',
                            '<%= app.source %>/html/friends/friends.js',
                            '<%= app.source %>/html/friends/friends.service.js',
                            '<%= app.source %>/html/top-users/top-users.js',
                            '<%= app.source %>/html/top-users/top-users.service.js',
                            '<%= app.source %>/html/word-set/word-set.js',
                            '<%= app.source %>/html/word-set/list/word-set.list.js',
                            '<%= app.source %>/html/word-set/word-set.service.js',
                            '<%= app.source %>/html/global-voc/global-voc.js',
                            '<%= app.source %>/html/global-voc/global-voc.service.js',
                            '<%= app.source %>/html/friends/social-networks/social-networks-friends.js',
                            '<%= app.source %>/html/devtab/devtab.js',
                            '<%= app.source %>/html/create-knowledge/directive/filter-points-button.directive.js',
                            '<%= app.source %>/html/create-knowledge/directive/eng-val-validator.directive.js',

                            // Training
                            '<%= app.source %>/html/training/training-service.js',
                            '<%= app.source %>/html/training/training.js',
                            '<%= app.source %>/html/training/eng-rus/eng-rus-training.directive.js',
                            '<%= app.source %>/html/training/def-eng/def-eng-training.directive.js',
                            '<%= app.source %>/html/training/rus-eng/rus-eng-training.directive.js',
                            '<%= app.source %>/html/training/img-eng/img-eng-training.directive.js',
                            '<%= app.source %>/html/training/sen-eng/sen-eng-training.directive.js',
                            '<%= app.source %>/html/training/directive/training-hint.directive.js',
                            '<%= app.source %>/html/training/directive/training-answer.directive.js',
                            '<%= app.source %>/html/training/directive/training-thumbnail-bottom.directive.js',

                            // Shared directives: Begin
                            '<%= app.source %>/html/directive/press-enter.directive.js',
                            '<%= app.source %>/html/directive/img-err-src.directive.js',
                            '<%= app.source %>/html/directive/say-text.directive.js',
                            '<%= app.source %>/html/directive/training-calendar.directive.js',
                            '<%= app.source %>/html/directive/alphabet.directive.js',

                            '<%= app.source %>/html/service/modal.service.js',
                            '<%= app.source %>/html/service/speech-synthesis.service.js',
                            '<%= app.source %>/html/service/training-calendar.service.js',
                            '<%= app.source %>/html/service/work.service.js',
                            '<%= app.source %>/html/service/work-effort.service.js'
                        ]
                    }
                }
            },
            // https://github.com/gruntjs/grunt-contrib-cssmin
            // https://github.com/jakubpawlowicz/clean-css#how-to-use-clean-css-api
            cssmin: {
                options: {
                    shorthandCompacting: false,
                    roundingPrecision: -1,
                    keepSpecialComments: false
                },
                awsS3_vendor_css: {
                    files: {
                            '<%= app.awsS3_dist %>/css/vendor_css_0.8.css': [
                            'bower_components/components-font-awesome/css/font-awesome.min.css',
                            'aws-s3/css/fonts-google.css',
                            'bower_components/bootstrap/dist/css/bootstrap.min.css',
                            'bower_components/angular-bootstrap/ui-bootstrap-csp.css',
                            'bower_components/normalize.css/normalize.css'
                        ]
                    }
                },
                awsS3_app_css: {
                    files: {
                        '<%= app.awsS3_dist %>/css/app/<%=app_css%>': [
                            '<%= app.source %>/css/h4t-eng.app.css'
                        ],
                        '<%= app.awsS3_dist %>/css/app/<%=app_login_css%>': [
                            '<%= app.source %>/css/h4t-eng.login.app.css'
                        ]
                    }
                }
            },
            // https://github.com/gruntjs/grunt-contrib-clean
            clean: {
                options: {
                    force: true
                },
                awsS3: {
                    src: ['<%= app.awsS3_dist %>']
                },
                prod_files: {
                    src: [
                        '<%=app.dist%>/js',
                        '<%=app.dist%>/css'
                    ]
                }
            },

            // https://github.com/gruntjs/grunt-contrib-htmlmin
            htmlmin: {
                // Target
                buildProd: {
                    // Target options
                    options: {
                        removeComments: true,
                        collapseWhitespace: true,
                        minifyJS: true
                    },
                    // Dictionary of files
                    files: {
                        // 'destination': 'source'
                        '<%= indexHtml %>': '<%= indexHtml %>',
                        '<%= loginHtml %>': '<%= loginHtml %>'
                    }
                }
            },

            // https://github.com/damkraw/grunt-gitinfo
            gitinfo: {
            },

            // https://github.com/slawrence/grunt-properties-reader
            properties: {
                dev_prop : '../+resources/dev/social-network.properties',
                prod_prop: '../+resources/prod/social-network.properties'
            },

            // https://github.com/eruizdechavez/grunt-string-replace
            'string-replace': {
                dev: {
                    files: {
                        '<%= loginHtml %>' : '<%= loginHtml %>',
                        '<%= indexHtml %>' : '<%= indexHtml %>'
                    },
                    options: {
                        replacements: [
                            // VK
                            {pattern: '$vk-client-id$', replacement: '<%=dev_prop[\'vk-client-id\']%>'},
                            {pattern: '$vk-redirect-url$', replacement: '<%=dev_prop[\'vk-redirect-url\']%>'},
                            // Facebook
                            {pattern: '$fb-client-id$', replacement: '<%=dev_prop[\'fb-client-id\']%>'},
                            {pattern: '$fb-redirect-url$', replacement: '<%=dev_prop[\'fb-redirect-url\']%>'},
                            // Git version
                            {pattern: /\$build_version\$/g, replacement: '<%=buildVersion%>'},
                            // CSS file
                            {pattern: '$app_css$', replacement: 'css/app/<%=app_css%>'},
                            {pattern: '$app_login_css$', replacement: 'css/app/<%=app_login_css%>'}
                        ]
                    }
                },
                prod: {
                    files: {
                        '<%= loginHtml %>' : '<%= loginHtml %>',
                        '<%= indexHtml %>' : '<%= indexHtml %>'
                    },
                    options: {
                        replacements: [
                            // VK
                            {pattern: '$vk-client-id$', replacement: '<%=prod_prop[\'vk-client-id\']%>'},
                            {pattern: '$vk-redirect-url$', replacement: '<%=prod_prop[\'vk-redirect-url\']%>'},
                            // Facebook
                            {pattern: '$fb-client-id$', replacement: '<%=prod_prop[\'fb-client-id\']%>'},
                            {pattern: '$fb-redirect-url$', replacement: '<%=prod_prop[\'fb-redirect-url\']%>'},
                            // Git version
                            {pattern: /\$build_version\$/g, replacement: '<%=buildVersion%>'},
                            // CSS file
                            {pattern: '$app_css$', replacement: 'https://d2ce9r2khtuixp.cloudfront.net/css/app/<%=app_css%>'},
                            {pattern: '$app_login_css$', replacement: 'https://d2ce9r2khtuixp.cloudfront.net/css/app/<%=app_login_css%>'},
                            // JS section
                            {
                                pattern: /<!--##begin_app_js##-->(.|\n|\r)*<!--##end_app_js##-->/i,
                                replacement: '<script src="https://d2ce9r2khtuixp.cloudfront.net/js/app/h4t-eng.app-<%=buildVersion%>.js"></script>'
                            }
                        ]
                    }
                },
                system_settings_app_properties: {
                    files: {
                        '<%= app.dist %>/WEB-INF/classes/application.properties' : '<%= app.dist %>/WEB-INF/classes/application.properties'
                    },
                    options: {
                        replacements: [
                            {pattern: '$build_version$', replacement: '<%=buildVersion%>'}
                        ]
                    }
                }
            },
            exec: {
                install_mvn_production_module : {
                    command: 'mvn clean install',
                    stdout: false,
                    options: {
                        cwd: '<%= app.workspace_production_path %>'
                    }
                },
                replace_args: {
                    command: '%JAVA_HOME_8%/bin/java -classpath h4t-eng-production-1.1.jar guru.voc.grunt.A1',
                    options: {
                        cwd: '<%= app.workspace_production_path %>/target'
                    }
                },
                echo_grunt_version: {
                    command: function () {
                        var a = grunt.config.get('gitinfo').local.branch.current.shortSHA;
                        var b = grunt.config.get('gitinfo').local.branch.current.lastCommitNumber;

                        var c = grunt.config.get('prod')['vk-client-id'];

                        return 'echo ' +  a + " : " +  b + " c: " + c;
                    }
                }
            }

        });


    grunt.task.registerTask('init_variables', '', function() {
        var buildVersion = grunt.config.get('gitinfo').local.branch.current.shortSHA + '-' + grunt.config.get('gitinfo').local.branch.current.lastCommitNumber;
        console.log('Use: BuildVersion: ' + buildVersion);
        grunt.config.set('buildVersion', buildVersion);

        grunt.config.set('indexHtml', grunt.config.get('app.dist') + '/index.html');
        grunt.config.set('loginHtml', grunt.config.get('app.dist') + '/login.html');

        grunt.config.set('app_css', 'h4t-eng.app-' + buildVersion + '.css');
        grunt.config.set('app_login_css', 'h4t-eng.login.app-' + buildVersion + '.css');

        console.log('Use: indexHtml: ' + grunt.config.get('indexHtml'));
        console.log('Use: loginHtml: ' + grunt.config.get('loginHtml'));

        console.log('Use: app_css: ' + grunt.config.get('app_css'));
        console.log('Use: app_login_css: ' + grunt.config.get('app_login_css'));


    });
    grunt.task.run('gitinfo', 'init_variables');


    grunt.registerTask('dev', [
        'copy:common_properties_files',
        'copy:dev_properties_files',
        'copy:css_file',
        'copy:static_html',
        'string-replace:system_settings_app_properties',
        'properties:dev_prop',
        'string-replace:dev',
        //'htmlmin:buildProd',
        'html2js',
        'copy:dev_js_files'
    ]);

    grunt.registerTask('prod', [
        'copy:common_properties_files',
        'copy:prod_properties_files',
        'copy:css_file',
        'copy:static_html',
        'string-replace:system_settings_app_properties',
        'properties:prod_prop',
        'string-replace:prod',
        'htmlmin:buildProd',
        'clean:prod_files'
    ]);

    grunt.registerTask('awsS3', [
        'clean:awsS3',
        // 'uglify:awsS3_vendor_js', // it's long operation, comment it when it needed
        'cssmin:awsS3_vendor_css',
        'cssmin:awsS3_app_css',
        'copy:awsS3_fonts',
        // make app js
        'html2js',
        'uglify:awsS3_app_js',
        'clean:prod_files'
    ]);

    grunt.registerTask('s1', ['properties:prod', 'gitinfo', 'exec:echo_grunt_version' ]);
};