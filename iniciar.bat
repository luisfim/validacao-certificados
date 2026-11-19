@echo off
cd /d "%~dp0"
if exist target\validacao-certificados.jar (
  java -jar target\validacao-certificados.jar
) else if exist bin\validacao-certificados.jar (
  java -jar bin\validacao-certificados.jar
) else (
  call mvnw.cmd -q -DskipTests package
  if errorlevel 1 (pause & exit /b 1)
  java -jar target\validacao-certificados.jar
)
pause
