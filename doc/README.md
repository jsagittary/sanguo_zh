#  生产环境下的小细节

* 热修复相关
    
    1. 热修复的代码需要外部引入的jar包(game-agent.jar)  
    存放位置:zombie\mw_game\src\main\resources\lib\game-agent.jar
    2. 需要将jar包安装到仓库.进入到jar的目录.  
    3. 执行如下命令:mvn install:install-file -Dfile=game-agent.jar -DgroupId=com.gryphpoem.game.zw -DartifactId=hotfix -Dversion=1.0.0 -Dpackaging=jar

