SET PY=C:\KanziWorkspace_3_6_4_12\3rdPartySDKs\Python32\python.exe
SET GENERATOR=%KANZI_CONNECT_SDK%\interfaces\generate.py

%PY% %GENERATOR% definitions\sensor_interface.xml ..\app\src\main\java\com\rightware\connect\sensor user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\media_interface.xml  ..\app\src\main\java\com\rightware\connect\media user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\weather_interface.xml  ..\app\src\main\java\com\rightware\connect\weather user --javaservice --javaservicestub
%PY% %GENERATOR% definitions\abc123_interface.xml  ..\app\src\main\java\com\rightware\connect\abc123 user --javaservice --javaservicestub

pause
