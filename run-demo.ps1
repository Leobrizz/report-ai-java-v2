$argsLine = '--cucumber=examples/cucumber/cucumber-report.json --allure=examples/allure-results --output=output --project=Demo-QA --env=UAT --framework=Cucumber+Selenium --ai.mode=mock'
mvn clean compile exec:java "-Dexec.args=$argsLine"
