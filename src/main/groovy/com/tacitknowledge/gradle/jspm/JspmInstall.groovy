package com.tacitknowledge.gradle.jspm

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils

class JspmInstall extends JspmTask
{
  JspmInstall()
  {
    description = "Resolve and download jspm dependencies"

    project.afterEvaluate{
      inputs.file new File("${project.projectDir}/${project.jspm.packageConfigPath}/package.json")
      inputs.file new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json")
      outputs.dir new File("${project.buildDir}/${project.jspm.buildDir}/assets/jspm_packages")
    }
  }

  @Override
  void exec()
  {
    project.delete("${project.buildDir}/${project.jspm.buildDir}/assets/jspm-config.js")
    FileUtils.copyFile(new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json"), new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json_bak"))
    args = ['dl-loader', project.jspm.loader, '-y']
    super.exec()
    FileUtils.forceDelete(new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json"))
    FileUtils.moveFile(new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json_bak"), new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json"))

    def config = new JsonSlurper().parse(new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json"))

    args = ['install', '-y']
    super.exec()

    //install overrides
    //this is temporary solution until jspm fixes it's override flow.
    config?.overrides?.each { k, v ->
      args = ['install', k, '-o', JsonOutput.toJson(v)]
      super.exec()
    }
  }
}
