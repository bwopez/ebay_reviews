package ebay_test_package;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteToCSV {
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining("|"));
	}
	
	public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines, String name, String typeOfFeedback) throws IOException {
		String fileName = name + "_" + typeOfFeedback;
		String directoryName = "D:\\matthews_desktop\\files\\Java_stuff\\selenium\\" + name;
	    File directory = new File(directoryName);
	    if (! directory.exists()){
	        directory.mkdir();
	        // If you require it to make the entire directory path including parents,
	        // use directory.mkdirs(); here instead.
	    }
	    File csvOutputFile = new File("D:\\matthews_desktop\\files\\Java_stuff\\selenium\\" + name + "\\" + fileName + "Reviews.csv");
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
	        dataLines.stream()
	          .map(this::convertToCSV)
	          .forEach(pw::println);
	    }
	}
	
	public String escapeSpecialCharacters(String data) {
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
	        data = data.replace("\"", "\"\"");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}
}
