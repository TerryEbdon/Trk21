@echo off
REM Terry Ebdon, April 2019
REM Convenient way to run single tests, with the testsuffix being optional
SETLOCAL ENABLEEXTENSIONS
set xca=%1
set xc=%xca:~-4%

if "%xc%" == "Test" (
  set testClass=%1
) else (
  set testClass=%xca%Test
)
echo testing... %testClass%
call gradlew --console=plain test --tests="net.ebdon.trk21.%testClass%"
