package RMI;

public class MessageProcessor implements Runnable{
	
	private GameServer gameserver;
	
	
	public MessageProcessor(GameServer gs){
		this.gameserver = gs;
		
	}
	
	@Override
	public void run(){
		Message msg;
		while(true){
			if((msg=this.gameserver.nextMessgae()) != null){
				//process msg 
				gameserver.processMessage(msg);
			}
		}
	}
}
