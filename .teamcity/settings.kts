import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.triggers.vcs

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

version = "2025.03"

project {
    description = "ADM-128054"

    buildType(PublishToSpace)
}

object PublishToSpace : BuildType({
    name = "Publish to space"

    params {
        password("env.SPACE_PASSWORD", "credentialsJSON:cac278db-1591-4ad6-9fc4-4ecef5f5e853")
        password("env.SPACE_USERNAME", "credentialsJSON:c038a60b-6747-4938-b725-4cfb201890a5")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            name = "Show credentials"
            id = "Show_creds"
            tasks = ":showCreds"
        }
        gradle {
            name = "Publish space"
            id = "Publish_space"
            tasks = "ai-dev-kit-trace-gradle:publish ai-dev-kit-trace-plugin:publish :publishContentModules"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:main"
        }
    }

    features {
        perfmon {
        }
    }
})
