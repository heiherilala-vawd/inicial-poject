cd build/gen && mvn clean install

set SRC=.\src\main\java\com\example\demo\client
set DEST=..\..\src\main\java\com\example\demo\client

echo 📦 Suppression de l'ancien dossier...
if exist "%DEST%" (
    rmdir /s /q "%DEST%"
)

echo 📁 Copie du nouveau dossier...
mkdir "%DEST%" 2>nul
xcopy "%SRC%" "%DEST%" /E /I /Y

echo ✅ Copie terminée avec succès.
endlocal
pause