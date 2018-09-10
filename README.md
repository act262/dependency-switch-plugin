### What
1. 主要用在模块化开发的项目中，将依赖的构建切换到对应的源码工程，方便修改和调试代码
2. 方便对三方库的调试和修改

### Usage
在`settings.gradle`中添加配置
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.jfz.plugin:dependency-switch:0.0.1'
    }
}

apply plugin: 'com.jfz.plugin.dependency-switch'
```
然后在同级目录下创建一个`dependency.json`文件，参考sample中写法
默认使用`dependency.json`，当然也可以在`gradle.properties`中指定文件名
```properties
# custom file name
dependency_switch=dependency_custom.json
```
**NOTE: 根节点是数组**

`dependency.json`
```json
[
  {
    "name": "matisse",
    "dir": "../Matisse/matisse",
    "open": true
  },
  {
    "name": "library",
    "dir": "sample/library_source/library"
  }  
]
```

> `name` 字段表示构建名，与构建名一致
> 
> `dir` 该构建物对应的源码工程目录，使用file的常规写法
> 
> `open` (可选)默认为true，因为json下不能使用注释，故此字段表示是否开启切换功能

最后sync一下即可出现对应的module

### Tip
1. 建议所有的模块工程和当前工程在同一父目录下，方便统一路径管理,更好的是使用相对路径
2. 建议公司内部的所有模块工程统一管理工程版本号、发布脚本等，方便引用到当前项目时不用做修改适配
3. 建议`dependency.json`加入到`.gitignore`中，如果路径统一可无需此操作