package g419.liner2.api.filter;

import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;


public class FilterBeforeSie extends Filter {
	
	public FilterBeforeSie(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
		this.appliesTo.add("CITY_NAM");
		this.appliesTo.add("COUNTRY_NAM");
		this.appliesTo.add("ROAD_NAM");
	}
	
	@Override
	public String getDescription() {
		return "Remove if there is 'się' after annotation";
	}

	@Override
	public Annotation pass(Annotation chunk, CharSequence cSeq) {
		List<Token> tokens = chunk.getSentence().getTokens();
		// jeśli po chunku nic nie ma
		if (chunk.getEnd() == tokens.size()-1)
			return chunk;
		// jeśli po chunku jest "się"
		if (tokens.get(chunk.getEnd() + 1).getOrth().equals("się"))
			return null;
		else
			return chunk;
	}

}
