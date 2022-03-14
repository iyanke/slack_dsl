import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.qodana
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubConnection
import jetbrains.buildServer.configs.kotlin.projectFeatures.slackConnection
import jetbrains.buildServer.configs.kotlin.triggers.schedule
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {

    vcsRoot(HttpsGitJetbrainsSpaceTcqaTcKilinaTestGitRefsHeadsMain)
    vcsRoot(HttpsGithubComIyankeJavaEclipse)

    buildType(FailedBuild)

    params {
        param("test", "test")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_147"
            name = "Docker Registry Name"
            url = "https://docker.io"
        }
        slackConnection {
            id = "PROJECT_EXT_148"
            displayName = "Jetbrains Slack (correct1)"
            botToken = "credentialsJSON:3bd4b204-bd7b-4aab-8d25-5b08fbb1358e"
            clientId = "2280447103.1062456421877"
            clientSecret = "credentialsJSON:da1b272a-27db-4a67-afc0-a32929329f7e"
        }
        githubConnection {
            id = "PROJECT_EXT_150"
            displayName = "GitHub.com"
            clientId = "55bff3b99533a7e68ae0"
            clientSecret = "credentialsJSON:1e885def-cc54-4314-a5e7-032ff995fc31"
        }
    }
}

object FailedBuild : BuildType({
    name = "Slack successful build"

    params {
        param("teamcity.ui.settings.readOnly", "false")
    }

    vcs {
        root(HttpsGithubComIyankeJavaEclipse)
    }

    steps {
        maven {
            enabled = false
            goals = "clean test"
            pomLocation = "java_eclipse/pom.xml"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            jdkHome = "%env.JDK_18%"
        }
        script {
            scriptContent = "echo yesy"
        }
        script {
            enabled = false
            executionMode = BuildStep.ExecutionMode.ALWAYS
            scriptContent = "echo ##teamcity[buildStatus status='SUCCESS' text='there is custom status for the build']"
        }
        qodana {
            linter = jvm1 {
            }
        }
    }

    triggers {
        vcs {
            enabled = false
        }
        schedule {
            enabled = false
            schedulingPolicy = cron {
                seconds = "0,30"
                minutes = "*"
            }
            triggerBuild = always()
            withPendingChangesOnly = false
            enableQueueOptimization = false
        }
        schedule {
            enabled = false
            schedulingPolicy = cron {
                seconds = "0,30,50"
                minutes = "*"
            }
            branchFilter = """
                +:<default>
                +:refs/heads/123
                +:refs/heads/bbbb
            """.trimIndent()
            triggerBuild = always()
            withPendingChangesOnly = false
            enableQueueOptimization = false
        }
    }

    features {
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_148"
                sendTo = "#slack-notifications-qa"
                messageFormat = verboseMessageFormat {
                    addBranch = true
                    addChanges = true
                    addStatusText = true
                    maximumNumberOfChanges = 11
                }
            }
            branchFilter = "+:*"
            buildStarted = true
            buildFailed = true
            buildFinishedSuccessfully = true
            firstBuildErrorOccurs = true
        }
        feature {
            type = "JetBrains.SonarQube.BranchesAndPullRequests.Support"
            enabled = false
            param("provider", "GitHub")
        }
    }
})

object HttpsGitJetbrainsSpaceTcqaTcKilinaTestGitRefsHeadsMain : GitVcsRoot({
    name = "https://git.jetbrains.space/tcqa/tc/kilina-test.git#refs/heads/main"
    url = "https://git.jetbrains.space/tcqa/tc/kilina-test.git"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "x-oauth-basic"
        password = "credentialsJSON:76927a3d-9cc7-4481-9107-6e2d75b7b69b"
    }
})

object HttpsGithubComIyankeJavaEclipse : GitVcsRoot({
    name = "https://github.com/iyanke/java_eclipse"
    url = "https://github.com/iyanke/java_eclipse"
    branch = "refs/heads/master"
    branchSpec = "+:*"
    authMethod = password {
        password = "credentialsJSON:530b0051-1215-4173-8cf0-2484f90facd1"
    }
    param("useAlternates", "true")
})
