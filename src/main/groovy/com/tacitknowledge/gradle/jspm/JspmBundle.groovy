package com.tacitknowledge.gradle.jspm

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.file.FileCollection

/**
 * See complete list of arithmetic features here: https://www.npmjs.com/package/systemjs-builder
 */
class JspmBundle extends JspmTask
{
  final FileCollection inputFiles

  JspmBundle()
  {
    description = 'Bundles all JSPM modules according to configuration'

    inputFiles = project.fileTree("${project.buildDir}/${project.jspm.buildDir}/assets").matching {
      exclude 'bundles/**/*'
      exclude 'jspm-config.js'
    }

    project.afterEvaluate{
      inputs.files inputFiles
      outputs.dir project.file("${project.buildDir}/${project.jspm.buildDir}/assets/bundles")
    }
  }

  @Override
  void exec()
  {
    def configFile = new File("${project.buildDir}/${project.jspm.buildDir}/assets/jspm-config.js")
    configFile.text = configFile.text + '''
      System.config({
        meta: {
          "inline/*": {
            "build": false
          }
        },
        "baseURL": "../assets",
        "packages": {
          "app": {
            "format": "esm",
            "defaultExtension": "js",
            "meta": {
              "*.js": {
                "babelOptions": {
                  "plugins": [
                    "babel-plugin-transform-react-jsx"
                  ]
                }
              }
            }
          }
        }
      });
    '''


    project.jspm?.bundles?.each { el ->
      println "Doing bundle for: [$el.definition] - [$el.bundleName]"
      args = [el.mode, el.definition, "../assets/bundles/${el.bundleName}.${project.jspm.uid}.js"] + el.args
      super.exec()
    }

    configFile.text = configFile.text.replace("\"/jspm_packages/", "\"/assets/jspm_packages/")
    configFile.text = configFile.text.replace("\"bundles/", "\"/assets/bundles/")

    //minify config
    configFile.text = configFile.text.readLines().collect{ it.trim() }.join('')
  }
}
