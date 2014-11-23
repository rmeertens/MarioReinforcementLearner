package radboud.meertens;

public class ActionModificatorFactory {
	public static final int HTTPMARIOSERVER = 0; 
	public static final int KEYBOARDCONTROLS = 1;
	public static ActionModificatorInterface getActionModificator(int actionModificator) throws Exception
	{
		// TODO: this is super super ugy!
		if(actionModificator == HTTPMARIOSERVER)
		{
			return new HTTPMarioServer();
		}
		throw new NullPointerException();
		
	}
}
