package radboud.meertens;

public class ActionModificatorFactory {

	public static ActionModificatorInterface getActionModificator() throws Exception
	{
		//return new TwitchObject2();
		return new HTTPMarioServer();
	}
}
