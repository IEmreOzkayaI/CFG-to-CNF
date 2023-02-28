import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Collections;

public class Test {

	static List<String> alphabet = new ArrayList<String>();
	static HashMap<String, List<String>> hashMapTemp = new HashMap<String, List<String>>();
	static String rhs = "";
	static StringBuilder terminals;

	public static void main(String[] args) throws FileNotFoundException {
		Scanner scan = new Scanner(new File("file.txt"));

		for (char c = 'A'; c <= 'Z'; ++c) {
			alphabet.add(String.valueOf(c));
		}

		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

		rhs = scan.nextLine().split("=")[1];
		terminals = new StringBuilder(rhs);

		while (scan.hasNextLine()) {
			// split left and right
			String[] lineElements = scan.nextLine().split("-");

			// find left hand side element
			String lineKey = lineElements[0];

			// find and split right hand side element
			String[] lineValues = lineElements[1].split("\\|");

			// add them to a list
			List<String> lineValuesList = new ArrayList<String>();
			for (int j = 0; j < lineValues.length; j++)
				lineValuesList.add(lineValues[j]);
			hashMap.put(lineKey, lineValuesList);
			if(lineValuesList.contains("S")) {
				List<String> ls = new ArrayList<String>();
				ls.add("S");
				hashMap.put("S0", ls);
			}
			alphabet.remove(lineKey);
		}

		// hashTableControl
		System.out.println("CFG Form");
		printHash(hashMap);
		System.out.println("\n------Eliminate €----");
		removeEpsilon(hashMap);
		printHash(hashMap);
		System.out.println("\n------Eliminate Unit Product----");
		removeUnitProduction(hashMap);
		//Control Generating
		controlGenerating(hashMap);
		//Control Reachable
		controlReachable(hashMap);
		printHash(hashMap);
		System.out.println("\n------Eliminate Terminals----");
		removeTerminals(hashMap);
		printHash(hashMap);
		printHash(hashMapTemp);
		System.out.println("\n------Break Variables That Are Longer Than Two------\n\t\t----CNF------");
		breakVariables(hashMap);
		printHash(hashMap);
		printHash(hashMapTemp);

	}

	private static void controlReachable(HashMap<String, List<String>> hashMap) {
		List<String> willRemoveKey = new ArrayList<String>();
		boolean flag = false;
		for (String key : hashMap.keySet()) {
			flag=false;
			for (Map.Entry<String, List<String>> value : hashMap.entrySet()) {
				for (int i = 0; i < value.getValue().size(); i++) {
						if(value.getValue().get(i).contains(key) || key.equalsIgnoreCase("S") || key.equalsIgnoreCase("S0")) {
							flag=true;
							break;
					}
				}
				if(flag) {
					break;
				}
			}
			if(!flag) {
//				System.out.println("non-reachable : "+ key );
				willRemoveKey.add(key);
			}
		}		
		
		for (String key : willRemoveKey) {
			hashMap.remove(key);
		}
	}

	private static void controlGenerating(HashMap<String, List<String>> hashMap) {
		for (Map.Entry<String, List<String>> value : hashMap.entrySet()) {
				for (int i = 0; i < value.getValue().size(); i++) {
					for (int j = 0; j < value.getValue().get(i).length(); j++) {
						if(!hashMap.keySet().contains(String.valueOf(value.getValue().get(i).charAt(j))) 
								&& terminals.indexOf(String.valueOf(value.getValue().get(i).charAt(j))) == -1 && value.getValue().get(i).charAt(j) !='€'  ) {
//							System.out.println("non-generating : "+ value.getKey() +"->" + value.getValue().get(i));
							value.getValue().remove(i);
							if(i!=0) {
								i = i-1;
							}else {
								break;
							}
						}
					}
				}
			}
	}

	private static void removeUnitProduction(HashMap<String, List<String>> hashMap) {
		for (Map.Entry<String, List<String>> value : hashMap.entrySet()) {
			for (Map.Entry<String, List<String>> key : hashMap.entrySet()) {
				if (value.getValue().contains(key.getKey()) && !value.getKey().equals(key.getKey())) {
					for (int i = 0; i < key.getValue().size(); i++) {
						value.getValue().add(key.getValue().get(i));
					}
					value.getValue().remove(key.getKey());
				}
				if (value.getKey().equals(key.getKey())) {
					value.getValue().remove(key.getKey());
				}
			}
		}

	}

	private static void removeTerminals(HashMap<String, List<String>> hashMap) {

		for (Map.Entry<String, List<String>> value : hashMap.entrySet()) {
			for (int i = 0; i < value.getValue().size(); i++) {
				if (value.getValue().get(i).length() > 1) {
					for (int j = 0; j < value.getValue().get(i).length(); j++) {
						if (!hashMap.keySet().contains(String.valueOf(value.getValue().get(i).charAt(j)))) {

							boolean containsValue = false; // terminale karþýlýk gelen harf olup olmadýðýný tespit eder.
							for (List<String> list : hashMapTemp.values()) {
								if (list.contains(String.valueOf(value.getValue().get(i).charAt(j)))) {
									containsValue = true;
									break;
								}
							}

							if (!containsValue) { // eðer terminale karþýlýk yoksa
								Collections.shuffle(alphabet);
								String randomElement = alphabet.get(0);
								List<String> element = new ArrayList<>();
								if (terminals.indexOf(String.valueOf(value.getValue().get(i).charAt(j))) != -1) {
									element.add(String.valueOf(value.getValue().get(i).charAt(j)));
									if (!hashMapTemp.containsValue(element)) {
										hashMapTemp.put(randomElement, element);
										alphabet.remove(randomElement);
									}
								}
								for (List<String> tempValue : hashMapTemp.values()) {
									for (List<String> actValue : hashMap.values()) {
										for (int k = 0; k < actValue.size(); k++) {
											if (actValue.get(k).contains(tempValue.get(0))
													&& actValue.get(k).length() > 1) {
												String keyFromValue = findKeyByValue(hashMapTemp, tempValue);
												String replacedString = actValue.get(k).replaceAll(tempValue.get(0),
														keyFromValue);
												actValue.set(k, replacedString);
												break;
											}
										}
									}
								}

							}
						}
					}
				}

			}
		}
	}

	private static void removeEpsilon(HashMap<String, List<String>> hashMap) {
		ArrayList<String> nullableSet = new ArrayList<String>();

		// find current nullable
		for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
			if (entry.getValue().contains("€")) {
				// System.out.println(entry.getKey() + " " +entry.getValue());
				nullableSet.add(entry.getKey());
			}
		}

		while (!nullableSet.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					if (entry.getValue().get(i).length() <= 1 && !entry.getValue().get(i).equalsIgnoreCase("€")
							&& nullableSet.contains(entry.getValue().get(i))) {
						if (!entry.getValue().contains("€")) {
							entry.getValue().add("€");
						}
						if (!nullableSet.contains(entry.getKey())) {
							nullableSet.add(entry.getKey());
						}
					} else {
						for (int j = 0; j < entry.getValue().get(i).length(); j++) {
							if (nullableSet.contains(String.valueOf(entry.getValue().get(i).charAt(j)))) {
								StringBuilder sb = new StringBuilder(entry.getValue().get(i));
								String element = sb.deleteCharAt(j).toString();
								if (!entry.getValue().contains(element)) {
									entry.getValue().add(element);
								}
							}

						}
					}

				}
				if(!entry.getKey().equalsIgnoreCase("S0")) {
					entry.getValue().remove("€");
				}
			}
			nullableSet.remove(0);
		}

		// find which rhs contains nullable things
	}

	public static void printHash(HashMap<String, List<String>> hashMap) {
		List<String> keys = new ArrayList<String>(hashMap.keySet());
		for (int i = keys.size() - 1; i >= 0; i--) {
			System.out.print(keys.get(i) + "-");
			List<String> ls = hashMap.get(keys.get(i));
			for (int j = 0; j < ls.size(); j++) {
				if (j == ls.size() - 1) {
					System.out.print(ls.get(j));

				} else {
					System.out.print(ls.get(j) + "|");
				}

			}
			System.out.println();
		}
	}

	public static String truncateString(String str) {
		Collections.shuffle(alphabet);
		String randomElement = alphabet.get(0);
		alphabet.remove(randomElement);
		if (str.length() <= 2) {
			return str;
		}
		return truncateString(str.substring(0, str.length() - 2) + randomElement);
	}

	private static void breakVariables(HashMap<String, List<String>> hashMap) {
		// hashmap'in valuelarýný dolaþýp uzunluðu 2den fazla ise yeni bir deðiþkene at

		for (Map.Entry<String, List<String>> value : hashMap.entrySet()) {
			for (int i = 0; i < value.getValue().size(); i++) {
				// print value.getValue().get() by two characters each time
				if (value.getValue().get(i).length() > 2) {
					String lastTwo = value.getValue().get(i).substring(value.getValue().get(i).length() - 2);
					String breakedString = truncateString(value.getValue().get(i));
					List<String> element = new ArrayList<>();
					element.add(lastTwo);
					if (!hashMapTemp.containsValue(element)) {
						value.getValue().set(i, breakedString);
						hashMapTemp.put(breakedString.substring(1), element);
					} else {

						value.getValue().set(i,
								value.getValue().get(i).substring(0, 1) + findKeyByValue(hashMapTemp, element));

					}
				}
			}
		}
	}

	public static void printTwoCharString(String str) {
		for (int i = 0; i < str.length(); i += 2) {
			System.out.print(str.charAt(i));
			if (i + 1 < str.length()) {
				System.out.print(str.charAt(i + 1));
			}
			if (i + 2 < str.length()) {
				System.out.print(" ");
			}
		}
	}

	public static String findKeyByValue(HashMap<String, List<String>> map, List<String> value) {
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
