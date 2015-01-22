package swin.chat.util;

public interface Delegate<PARAM,RETURN> {
	
	public RETURN doStep(PARAM ...params );
}
