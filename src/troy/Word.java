package troy;


public class Word {
	public final Line line;
	public final String word;
	
	public Word(Line wordLine, String wordString) {
		assert(wordLine.length == wordString.length());
		line = wordLine;
		word = wordString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Word)) {
			return false;
		} else {
			Word other = (Word) obj;
			return other.line.equals(line) && other.word.equals(word);
		} 
	}
	
	@Override
	public String toString() {
		return word;
	}
}
