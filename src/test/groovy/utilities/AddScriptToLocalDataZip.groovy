package utilities

import org.junit.Test
import org.zeroturnaround.zip.FileSource
import org.zeroturnaround.zip.ZipEntrySource
import org.zeroturnaround.zip.ZipUtil

import static utilities.ResourcePath.resourcePath

class AddScriptToLocalDataZip {

    public static void addScriptToLocalDataZip(Class testClass, String scriptName, String scriptPath, String targetPath) {
        addScriptToLocalDataZip(testClass, scriptName, [scriptPath], targetPath)
    }

    public static void addScriptToLocalDataZip(Class testClass, List scriptNames, String scriptPath, String targetPath){
        def filesToAdd = [:]
        scriptNames.each {
            def scriptSource = new FileSource("${targetPath}/${it}", new File("${scriptPath}/${it}"))
            filesToAdd.put("${scriptPath}/${it}", scriptSource)
        }

        def directoryPath = resourcePath(testClass.getCanonicalName().replace('.', '/')) as String
        if(directoryPath == null){
            return
        }
        def directory = new File(directoryPath)

        testClass.getMethods().each {
            if(it.getAnnotation(Test) != null){
                def zipTestFiles = it.getAnnotation(ZipTestFiles)
                def zip = new File(directory, "${it.getName()}.zip")

                if(zipTestFiles != null) {
                    zipTestFiles.files().each() { filename ->
                        File file = new File(directory, filename)
                        filesToAdd.put(file.absolutePath, new FileSource(filename, file))
                    }
                }
                println("Creating ${zip.getName()} to hold ${filesToAdd}")
                ZipUtil.pack(filesToAdd.values() as ZipEntrySource[], zip)
            }
        }
    }
}


