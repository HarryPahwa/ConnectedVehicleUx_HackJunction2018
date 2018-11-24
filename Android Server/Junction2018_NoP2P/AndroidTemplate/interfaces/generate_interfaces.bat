SET PY=%PYTHON_HOME%\python.exe
SET GENERATOR=%KANZI_CONNECT_SDK%\interfaces\generate.py

%PY% %GENERATOR% definitions\sensor_interface.xml ..\app\src\main\java\com\rightware\connect\sensor user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\media_interface.xml  ..\app\src\main\java\com\rightware\connect\media user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\weather_interface.xml  ..\app\src\main\java\com\rightware\connect\weather user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\mapleVodka_interface.xml  ..\app\src\main\java\com\rightware\connect\mapleVodka user --javaservice --javaservicestub

pause
