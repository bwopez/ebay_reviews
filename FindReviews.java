package ebay_test_package;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FindReviews {
	public static void main(String[] args) {
		String userInput = getUserInput();
		String userUrl = getUrl(userInput);
		
		WebDriver driver = getDriver();
		driver.get(userUrl);
		extractReviews(driver, userInput);
		
		driver.quit();
		System.exit(0);
	}
	
	public static WebDriver getDriver() {
		/*
		 * This gets a new Firefox WebDriver with the correct properties
		 * 
		 * @return The Firefox WebDriver
		 */
		System.setProperty("webdriver.gecko.driver", "D:\\Downloads\\geckodriver-v0.30.0-win64\\geckodriver.exe");
		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true"); 
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
		WebDriver driver = new FirefoxDriver();
		
		return driver;
	}
	
	public static String getUserInput() {
		/*
		 * This gets the user's input
		 * 
		 * @return The user's input as a String
		 */
	    try (Scanner myObj = new Scanner(System.in)) {
			System.out.println("Enter username to extract reviews: ");
			String userInput = myObj.nextLine();  // Read user input
			
			return userInput;
		}
	}
	
	public static String getUrl(String userInput) {
		/*
		 * This returns the user's ebay page
		 * 
		 * @parameter userInput: the user's requested username
		 * 
		 * @return The url to navigate to
		 */
		return "https://www.ebay.com/usr/" + userInput;
	}
	
	public static void extractReviews(WebDriver driver, String userInput) {
		/*
		 * This extracts all of the information from the Neutral and Negative reviews
		 * 
		 * @parameter driver: The WebDriver that is controlling the browser
		 * @parameter userInput: The input from the user as a String
		 * 
		 * @return None
		 */
		
		// checking for invalid username
		try {
			if(driver.findElement(By.className("sm-md")).getText().contains("The User ID you entered was not found")) {
				System.out.println("User was not found. Please enter a valid username.");
				driver.quit();
				System.exit(0);
			}
		}
		catch(NoSuchElementException nsee) {
			System.out.println("Executing reviews scrape.");
		}
		
		WebElement feedbackRatings = driver.findElement(By.id("feedback_ratings"));
		List<WebElement> hrefs = feedbackRatings.findElements(By.tagName("a"));
		
		List<WebElement> reviewButtons = new ArrayList<>();
		for(int i = 0; i < hrefs.size(); i++) {
			reviewButtons.add(getElement(hrefs, i));
		}
		
		// click on neutral reviews
		reviewButtons.get(1).click();
		getInformation(driver, "Neutral", userInput);
		
		// click on negative reviews
		WebElement negativeButton = driver.findElement(By.cssSelector("button[data-test-id='rating-count-9'"));
		negativeButton.click();
		getInformation(driver, "Negative", userInput);
		
		System.out.println("Finished extracting reviews.");
	}
	
	public static WebElement getElement(List<WebElement> hrefs, int index) {
		/*
		 * This is to extract the specific WebElement to click on for the reviews buttons
		 * 
		 * @parameter hrefs: The list of WebElements to extract information from
		 * @parameter index: The correct index of the WebElements to work with
		 * 
		 * @return The clickable WebElement
		 */
		WebElement div = hrefs.get(index).findElement(By.tagName("div"));
		List<WebElement> allSpans = div.findElements(By.tagName("span"));
		
		return allSpans.get(2);
	}
	
	public static String retryGetText(WebElement element) {
	    int attempts = 0;
	    String elementInformation = "";
	    while(attempts < 2) {
	        try {
//	            driver.findElement(by).click();
	        	elementInformation = element.getText();
	            break;
	        } catch(StaleElementReferenceException e) {
	        }
	        attempts++;
	    }
	    return elementInformation;
	}
	
	public static void getInformation(WebDriver driver, String reviewType, String name) {
		/*
		 * This gathers all information for the reviews and writes them to csv file
		 * 
		 * @parameter driver: The WebDriver used to navigate
		 * @parameter reviewType: The type of review (Neutral, Negative)
		 * @parameter name: The name of the user
		 * 
		 * @return None
		 */
		WebDriverWait wait = new WebDriverWait(driver, 10); 
		WebElement nextPage = wait.until(ExpectedConditions.elementToBeClickable(By.id("next-page")));
		List<String[]> dataLines = new ArrayList<>();
		boolean nextClickable = nextPage.getAttribute("aria-disabled") == null;
		
		do {
			WebElement tbody = driver.findElement(By.id("feedback-cards")).findElement(By.tagName("tbody"));
			List<WebElement> trs = tbody.findElements(By.tagName("tr"));
			for(int i = 0; i < trs.size(); i++) {
				try {
					if(trs.get(i).getAttribute("data-feedback-id") != null) {
						List<WebElement> tds = trs.get(i).findElements(By.tagName("td"));
						WebElement feedbackTd = tds.get(0);
						WebElement fromTd = tds.get(1);
						WebElement whenTd = tds.get(2);
						
						// get feedback
//						String feedback = retryGetText(feedbackTd.findElement(By.className("card__feedback-container")).findElement(By.className("card__feedback")).findElement(By.className("card__comment")).findElement(By.tagName("span")));
						String feedback = feedbackTd.findElement(By.className("card__feedback-container")).findElement(By.className("card__feedback")).findElement(By.className("card__comment")).findElement(By.tagName("span")).getText();
						
						// get from
						String from = "N/A.";
						String price = "N/A.";
//						from = retryGetText(fromTd.findElement(By.className("card__from")).findElement(By.tagName("span")));
						from = fromTd.findElement(By.className("card__from")).findElement(By.tagName("span")).getText();
						try {
//							price = retryGetText(fromTd.findElement(By.className("card__price")).findElement(By.tagName("span")));
							price = fromTd.findElement(By.className("card__price")).findElement(By.tagName("span")).getText();
						}
						catch(NoSuchElementException nsee) {
							System.out.println("No price found for review.");
						}
						
						// get when
//						String when = retryGetText(whenTd.findElement(By.tagName("div")).findElement(By.tagName("span")));
						String when = whenTd.findElement(By.tagName("div")).findElement(By.tagName("span")).getText();
						
						// write information to csv
						dataLines.add(new String[] {feedback, from, price, when, reviewType});
						WriteToCSV writeToCSV = new WriteToCSV();
						try {
							writeToCSV.givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines, name, reviewType);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				catch(StaleElementReferenceException nsee) {
				System.out.println("We've run into a stale element reference.");
//				driver.navigate().refresh();
//				wait.until(ExpectedConditions.elementToBeClickable(By.id("next-page")));
				continue;
				}
			}
		
			nextPage = wait.until(ExpectedConditions.elementToBeClickable(By.id("next-page")));
			nextPage.click();
			nextClickable = nextPage.getAttribute("aria-disabled") == null;
		} while(nextClickable);
	}
}
