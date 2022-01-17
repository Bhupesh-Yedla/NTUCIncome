package NTUCIncomeTest;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.restassured.response.Response;


public class NTUCIncome {
	WebDriver driver;
	Map<String,List<String>> map=new HashMap<String,List<String>>();
	/**
	 * Description: API Automation
	 */
	@Test (enabled=true)
	public void GitHubAPI() throws ParseException {
		baseURI="https://github.com/";
		
		//Send Get request with username - torvalds
		Response response=given()
			.auth().none()
		.when()
			.get("/torvalds")
		.then()
			.extract()
			.response();
		
		//Display username and name for username - torvalds
		System.out.println("Username = "+response.htmlPath().getString("**.find {it.@class =='p-nickname vcard-username d-block'}").trim());
		System.out.println("Name = "+response.htmlPath().getString("**.find {it.@class =='p-name vcard-fullname d-block overflow-hidden'}").trim());
		
		//Send Get request with parameters - To retrieve created date
		response = given()
					.auth().none()
					.queryParam("tab", "overview")
					.queryParam("from", "2011-09-01")
					.queryParam("to", "2011-09-30")
				.when()
					.get("/torvalds")
				.then()
					.extract()
					.response();
		
		//Display created date for username - torvalds
		System.out.println("Created = "+response.htmlPath().getString("**.find{it.@class=='d-inline-block mb-2'}"));
		
		//Send Get request with parameters - To retrieve all repositories 
		response = given()
					.auth().none()
					.queryParam("tab", "repositories")
				.when()
					.get("/torvalds")
				.then()	
					.extract()
					.response();
		
		Document document = Jsoup.parse(response.getBody().asPrettyString());
		Elements elements = document.getElementsByAttributeValue("itemprop", "owns");
		
		//Display all repository information with stars and releases data.
		for(int i=0;i<elements.size();i++) {
			List<String> ls=new ArrayList<String>();
			System.out.println("Repository "+(i+1)+" = "+elements.get(i).getElementsByTag("a").get(0).text());
			System.out.println("\tStars = "+elements.get(i).select("[href*=stargazers]").text());
			response = given()
					.auth().none()
				.when()
					.get("/torvalds/"+elements.get(i).getElementsByTag("a").get(0).text())
				.then()	
					.extract()
					.response();
			
			document = Jsoup.parse(response.getBody().asPrettyString());
			String releases= document.select("[href*=releases]").select("span[class*=counter]").text();
			
			//Adding repository, stars and release info to ArrayList
			ls.add(elements.get(i).getElementsByTag("a").get(0).text());
			ls.add(elements.get(i).select("[href*=stargazers]").text());
			ls.add(releases.equalsIgnoreCase("")?"0":releases);
			
			//Storing List of repository details into a string of HashMap 
			map.put("repo_"+(i+1), ls);
			System.out.print("\tReleases = ");
			if(releases.equalsIgnoreCase(""))
				System.out.println(0);
			else
				System.out.println(releases);
		}
	}
/**
 * Description: Web UI Automation
 */	
	@BeforeTest
	public void setup() {
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\Bhupesh\\eclipse-workspace\\NTUCIncome\\src\\test\\resources\\chromedriver.exe");
	}
	
	@Test
	public void GitHubWebUI() {
		driver=new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		
		//Navigate to webpage
		driver.get("https://www.github.com");
		
		//Search username torvalds in github
		driver.findElement(By.xpath("//input[contains(@placeholder,'Search GitHub')]")).sendKeys("torvalds");
		driver.findElement(By.xpath("//div[@aria-label='torvalds']")).click();
		driver.findElement(By.xpath("//a[contains(text(),'Advanced search')]")).click();
		driver.findElement(By.xpath("//*[@id='adv_code_search']/div[1]/label/input")).clear();
		driver.findElement(By.xpath("//label[contains(text(),'From these owners')]/..//following-sibling::dd/input")).sendKeys("torvalds");
		driver.findElement(By.xpath("(//button[contains(text(),'Search')])[1]")).click();
		
		//Click on repository 1 of username torvalds - Linux
		driver.findElement(By.xpath("//a[contains(@href,'linux') and @class='v-align-middle']")).click();
		String stars = driver.findElement(By.xpath("//span[@id='repo-stars-counter-star']")).getText();
		
		//Display stars result for both UI and API for repo 1 Linux
		System.out.println("Linux Repo - Stars result from UI: "+stars);
		System.out.println("Linux Repo - Stars result from API: "+map.get("repo_1").get(1));
		
		//Asserting stars data from UI with API results
		Assert.assertTrue(map.get("repo_1").get(1).split(",")[0].contains(stars.replaceAll("[a-zA-Z]", "")),"Test has been failed - stars value not matched between UI and API result for repo Linux");
	}
	
	@AfterTest
	public void tearDown() {
		driver.close();
	}
}
