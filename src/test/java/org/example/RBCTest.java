package org.example;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.codeborne.selenide.Selenide.*;

/**
 * Selenoid (chrome+firefox+opera+safari+edge) + Selenide + Allure + jUnit5
 */
public class RBCTest //Про параллельный запуск тестов и запуск по меткам - https://automated-testing.info/t/junit5-maven-parallelnyj-zapusk-testov-i-zapusk-po-tegam/20805
{
    private static final String CHROME = "chrome";
    private static final String FIREFOX = "firefox";
    /*private static final String SAFARI = "safari";
    private static final String OPERA = "opera";
    private static final String EDGE = "edge";*/
    String selenoidUrl = "http://localhost:4444/wd/hub";

    DesiredCapabilities capabilities = new DesiredCapabilities(); //Статья по подключению Selenide к Selenoid https://habr.com/ru/post/473454/

    @BeforeEach
    public void start() throws Exception { //https://automation-remarks.com/2017/selenoid/index.html
        Configuration.baseUrl="https://www.rbc.ru";
        Configuration.remote = selenoidUrl; //Url удалённого веб драйвера

        capabilities.setBrowserName(CHROME);
        capabilities.setVersion("87.0");
        capabilities.setCapability("enableVNC", true); //VNC позволяет нам видеть браузер и взаимодействовать с ним + журнал
        capabilities.setCapability("enableVideo", false);
        capabilities.setCapability("enableLog", true);
        capabilities.setCapability("screenResolution", "1366x768x24");
        capabilities.setCapability("name", "TestRBCChrome");
        Configuration.browserCapabilities = capabilities; //Переопределяем Browser capabilities

        /*capabilities.setBrowserName(FIREFOX);
        capabilities.setVersion("82.0");
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", false);*/

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(true));
    }

    @Test
    @DisplayName("Пустой поиск")
    @Tag("search") //группировка тестов по функционалу
    @Severity(SeverityLevel.CRITICAL)
    @Feature("Search")
    void SearchEmpty(){
        open("/companies/");
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), 15, 100);
        wait.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector(".home__search-form .search-form__btn")))).click();
        $("h3").shouldHave(Condition.text("Все результаты")); }

    @RepeatedTest(2)
    @DisplayName("Поиск компании 'ВТБ'")
    @Story("Проверка работы поиска")
    @Feature("Search")
    @Tag("smoke") //mvn -Dtag=smoke test
    void SearchSpecificCompany (){
        open("/companies/");
        $(".home__search-form #query").val("ВТБ");
        $(".home__search-form .search-form__btn").click();
        $$(".company-card").shouldHave(CollectionCondition.size(20)); }

    @ParameterizedTest
    @ValueSource(strings = {"Сбербанк", "Ростелеком"})
    void SearchParamString (String argument){
        open("/companies/");
        $(".home__search-form #query").val(argument).pressEnter();
        $$(".company-card").shouldHave(CollectionCondition.size(20));}
}
//mvn clean test -Dbrowser=chrome -Dselenide.browserVersion=87.0 -Dthreads=2