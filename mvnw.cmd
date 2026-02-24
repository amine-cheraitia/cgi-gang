@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script, version 3.3.2
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM   https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET "MAVEN_CMD_LINE_ARGS=%*"

@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@IF NOT EXIST %WRAPPER_JAR% (
  @FOR /F "usebackq tokens=2 delims==" %%A IN (`findstr /C:"wrapperUrl" %WRAPPER_PROPERTIES%`) DO (
    @SET WRAPPER_URL=%%A
  )
  @ECHO Downloading Maven Wrapper: !WRAPPER_URL!
  @IF DEFINED JAVA_HOME (
    @SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
  ) ELSE (
    @SET "JAVA_EXE=java"
  )
  @curl -fsSL -o %WRAPPER_JAR% "!WRAPPER_URL!" 2>NUL || (
    @ECHO ERROR: Could not download maven-wrapper.jar. Ensure curl is available or download manually. >&2
    @EXIT /B 1
  )
)

@IF DEFINED JAVA_HOME (
  @SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  @SET "JAVA_EXE=java"
)

@FOR /F "usebackq tokens=2 delims==" %%A IN (`findstr /C:"distributionUrl" %WRAPPER_PROPERTIES%`) DO (
  @SET DISTRIBUTION_URL=%%A
)

@"%JAVA_EXE%" -jar %WRAPPER_JAR% "%MAVEN_PROJECTBASEDIR%" "%DISTRIBUTION_URL%" %MAVEN_CMD_LINE_ARGS%
