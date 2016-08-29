package com.hoosteen.waveform.graphics;

import java.util.ArrayList;

class FrameCounter{
	
	String title;
	
	public FrameCounter(String title){
		this.title = title;
	}
	
	Long lastTime = System.nanoTime();
	ArrayList<Long> frameList = new ArrayList<Long>();
	
	public void update(){
		long timePassed = System.nanoTime() - lastTime;
		lastTime += timePassed;
		
		frameList.add(timePassed);
		
		while(sum(frameList) >= 1e9){
			frameList.remove(0);
		}
		
	//	System.out.println(title + " : " + frameList.size() + "FPS");
	}
	
	public Long sum(ArrayList<Long> list){
		long total = 0;
		for(Long l : list){
			total += l;
		}
		return total;
	}

	public int getFPS() {
		return frameList.size();
	}
}