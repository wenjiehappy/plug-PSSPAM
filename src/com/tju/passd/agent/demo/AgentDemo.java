package com.tju.passd.agent.demo;

import java.util.List;

import com.tju.passd.agent.base.BaseAgent;
import com.tju.passd.agent.orderSerializable.Order;
import com.tju.passd.agent.orderSerializable.Trade;
import com.tju.passd.agent.util.DecideMethodSet;

public class AgentDemo extends BaseAgent {
	
	private double stockPrice;
	
	public AgentDemo(double initCash, int initHold, double availableCash, double lockedCash, int availableHold, int lockedHold ){
		super(initCash, initHold, availableCash, lockedCash, availableHold, lockedHold);
	}
	
	@Override
	public void agentRunningLogic() {
		
		stockPrice = getStockPrice();
		Order or = decision();
		String msgFromMarket = "";
		if( null != or ){
			msgFromMarket = submitOrder(or);
		}
		else{
			msgFromMarket = refreshMessage();
		}
		List<Trade> tradeList = resoveMessageForTradeList(msgFromMarket);
		//TODO do something about the tradeList
	}
	
	@Override
	public Order decision() {
		if( Math.random() < 0.3 )
			return null;
		else
			return DecideMethodSet.randomDecide(this, stockPrice);
	}
	
	public static void main(String[] args) {
		
		String marketIP = args[0];
		
		//int numberOfAgent = 20;
		int numberOfAgent = Integer.valueOf(args[1]);
		
		//final int recycleTime = 200;
		final int recycleTime = Integer.valueOf(args[2]);
		
		final String marketUrl = "http://" + marketIP + ":8080/stockMarket/Market?";
		final String registerUrl = "http://" + marketIP + "8080/stockMarket/Register?";
		for( int i =0; i < numberOfAgent; i++ ){
			new Thread(){
				@Override
				public void run() {
					super.run();
					BaseAgent agentDemo = new AgentDemo(1000, 0, 1000, 0, 0, 0);
					for( int i = 0; i < recycleTime; i++ ){
						agentDemo.simulate( marketUrl, registerUrl );
						try {
							Thread.sleep( 500 );
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		
	}
	
}
