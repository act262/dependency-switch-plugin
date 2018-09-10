package com.jfz.plugin.dsp

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Dependency switch
 * <p>
 *     Default action read from `dependency.json`
 *
 *     <pre>
 *         [
 *{"name": "matisse", "dir": "../Matisse/matisse"}*         ]
 *     </pre>
 * </p>
 *
 * 1. normal status using published artifact jar/aar
 * 2. source status using project's source
 */
class DependencySwitchPlugin implements Plugin<Settings> {

    private static final String PROPERTIES_DEP = 'dependency_switch'

    private static final String DEFAULT_DEP_FILE = 'dependency.json'

    private Settings settings

    @Override
    void apply(Settings settings) {
        this.settings = settings

        def depFile = new File(settings.rootDir,
                settings.hasProperty(PROPERTIES_DEP) ? settings[PROPERTIES_DEP] : DEFAULT_DEP_FILE)
        if (depFile == null || !depFile.exists()) {
            return
        }

        def jsonSlurper = new JsonSlurper()
        def list = jsonSlurper.parse(depFile) as List
        if (list == null || list.empty) {
            return
        }

        def depMap = list.grep { isValidProject(it) && isOpen(it) }
                .collectEntries {
            [it.name, it]
        }

        settings.gradle.settingsEvaluated {
            depMap.each {
                if (isValidProject(it.value)) {
                    settings.includeFlat("${it.key}")
                    settings.project(":${it.key}").projectDir = new File(it.value.dir)
                    println "Include project ${it.key}"
                }
            }
        }

        settings.gradle.afterProject { project ->
            project.configurations.each { conf ->
                def exclude = conf.dependencies.grep {
                    depMap.get(it.name)
                }

                exclude.each {
                    def depProject = project.project(":${it.name}")
                    conf.dependencies.remove(it)
                    project.dependencies.add(conf.name, depProject)
                    println "Inject dependencies project $depProject to replace module $it"
                }
            }
        }
    }

    static boolean isOpen(Map json) {
        json.get('open', true)
    }

    static boolean isValidProject(def it) {
        def dir = new File(it.dir)
        return dir != null && dir.exists()
    }
}