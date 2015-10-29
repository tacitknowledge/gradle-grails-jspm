package com.tacitknowledge.gradle.jspm
import com.moowork.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War

import java.nio.file.Paths

class JspmGrailsPlugin implements Plugin<Project>
{
  void apply(Project project)
  {
    project.plugins.apply(NodePlugin)

    project.extensions.create('jspm', JspmGrailsExtension)

    project.node.download = project.jspm.downloadNode

    project.tasks.with {
      def installJspm = create(
              name: 'installJspm',
              type: InstallJspm)

      def jspmMerge = create(
              name: 'jspmMerge',
              type: MergePackageDefinitionsTask,
              dependsOn: ['compileGroovy', 'compileJava'])

      def jspmInstall = create(
              name: 'jspmInstall',
              type: JspmInstall,
              dependsOn: [installJspm, jspmMerge])

      def gatherAssetsTask = create(
              name: "gatherAssetsForJspm",
              type: Copy,
              dependsOn: ['compileGroovy', 'compileJava']) {
        //from all plugin jars
        from {
          project.configurations.runtime.collect {
            project.zipTree(it).matching {
              include 'META-INF/assets/**'
              eachFile { details -> details.path = details.path - 'META-INF/assets' }
            }
          }
        }

        //from current application/plugin
        from "${project.projectDir}/grails-app/assets/javascripts"
        from "${project.projectDir}/grails-app/assets/stylesheets"
        from "${project.projectDir}/grails-app/assets/images"

        //into JSPM's build directory
        into "${project.buildDir}/${project.jspm.buildDir}/assets"
        includeEmptyDirs = false
      }

      def jspmBundle = create(
              name: 'jspmBundle',
              type: JspmBundle,
              dependsOn: [gatherAssetsTask, jspmInstall])


      //Wire into the process
      project.tasks.withType(War) { War bundleTask ->
        bundleTask.dependsOn jspmBundle
        bundleTask.from "${project.buildDir}/${project.jspm.buildDir}/assets/bundles", {
          into "assets/bundles"
        }
        bundleTask.from "${project.buildDir}/${project.jspm.buildDir}/assets/jspm_packages", {
          into "assets/jspm_packages"
        }
        bundleTask.from "${project.buildDir}/${project.jspm.buildDir}/assets/jspm-config.js", {
          into "assets"
        }
      }
      project.tasks.findByName('run')?.dependsOn jspmInstall
    }

    project.tasks.withType(Jar) { Jar bundleTask ->
      def jspmDir = Paths.get(project.projectDir.path, project.jspm.packageConfigPath).toFile()
      bundleTask.inputs.dir jspmDir
      bundleTask.from jspmDir, {
        into "META-INF/jspm"
      }
    }

    project.afterEvaluate {
      def packageJson = Paths.get(project.projectDir.path, project.jspm.packageConfigPath, 'package.json').toFile()
      if (!packageJson.exists())
      {
        initPackageJsonFile(packageJson)
      }
    }
  }

  static void initPackageJsonFile(File packageJson)
  {
    packageJson.parentFile.mkdirs()
    packageJson.text = """
      |{
      |  "directories": {
      |    "baseURL": "../assets"
      |  },
      |  "configFile": "../assets/jspm-config.js",
      |  "dependencies": {
      |  },
      |  "registry": "npm",
      |  "jspm": true
      |}""".stripMargin()
  }

}
