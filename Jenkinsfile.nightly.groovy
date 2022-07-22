pipeline {
    agent any

    tools {
        jdk 'openjdk-11'
    }

    environment {
        MVNW_ALIAS = "./mvnw --no-transfer-progress"
        NOTIFICATIONS_CHANNEL = '#authoring-poc-performance'
        ARTICLE_USERS = "${env.ARTICLE_USERS}"
        LIVE_ARTICLE_USERS = "${env.ARTICLE_USERS}"
        GALLERY_USERS = "${env.GALLERY_USERS}"
        TIME_MINUTES = "${env.TIME_MINUTES}"
    }

    stages {
        stage("Build Maven") {
            steps {
                sh '$MVNW_ALIAS clean'
            }
        }
        stage("Run Gatling") {
            steps {
                sh '$MVNW_ALIAS gatling:test -DarticleUsers=${ARTICLE_USERS} -DgalleryUsers=${GALLERY_USERS} -DliveArticleUsers=${LIVE_ARTICLE_USERS}  -DtotalTime=${TIME_MINUTES}'
            }
            post {
                always {
                    gatlingArchive()
                }
                success {
                    wrap([$class: 'BuildUser']) {
                        slackSend message: "Nightly Authoring Performance Tests Passed Link Here For Report \n" +
                                "https://jenkins-prod.api-platforms.telegraph.co.uk/job/Dashboard/job/Authoring%20Nightly%20Performance/$BUILD_NUMBER/gatling/",
                                token: env.SLACK_PLATFORMS_RELEASES, channel: env.NOTIFICATIONS_CHANNEL, color: 'good'
                    }
                }
                unsuccessful {
                    wrap([$class: 'BuildUser']) {
                        slackSend message: "Nightly Authoring Performance Tests failed Link Here For Report \n" +
                                "https://jenkins-prod.api-platforms.telegraph.co.uk/job/Dashboard/job/Authoring%20Nightly%20Performance/$BUILD_NUMBER/gatling/",
                                token: env.SLACK_PLATFORMS_RELEASES, channel: env.NOTIFICATIONS_CHANNEL, color: 'danger'
                    }
                }
            }
        }
    }
}