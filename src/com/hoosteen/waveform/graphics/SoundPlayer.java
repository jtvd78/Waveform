package com.hoosteen.waveform.graphics;

import java.io.File;
import java.util.ArrayList;

import com.hoosteen.waveform.Sound;

import com.hoosteen.waveform.*;

public class SoundPlayer {

	Sound sound;
	
	ArrayList<PlayerListener> listenerList = new ArrayList<PlayerListener>();
	
	public void addPlayerListener(PlayerListener listener){
		listenerList.add(listener);
	}
	
	private boolean isSound(String name){
		
		String fileType = name.substring(name.indexOf(".") + 1, name.length());
		
		if(fileType.equals("wav") || fileType.equals("mp3")){
			return true;
		}else{
			return false;
		}
	}
	
	public void skipBack() {
		File soundFile = new File(sound.getFilepath());
		String name = soundFile.getName();
		
		String lastSoundFound = null;
		
		System.out.println(soundFile);
		System.out.println(soundFile.getParentFile());
		
		File parent =  soundFile.getParentFile();
		String[] children = parent.list();
		
		for(String child : children){
			
			System.out.println(child + " : " + name);
			
			if(child.equals(name)){
				if(lastSoundFound != null){
					File lastSoundFile = new File(parent.getAbsolutePath() + File.separatorChar + lastSoundFound);
					play(lastSoundFile);
					break;
				}else{
					System.out.println("Last is null");
				}
			}
				
			if(isSound(child)){
				lastSoundFound = child;
			}	
			
			
		}
		
	}



	public void skipForward() {
		File soundFile = new File(sound.getFilepath());
		
		System.out.println("Filepath: " + sound.getFilepath());
		
		String name = soundFile.getName();
		
		boolean getNext = false;
		
		System.out.println(soundFile);
		File parent =  soundFile.getParentFile();
		System.out.println(parent);
		String[] children = parent.list();
		
		for(String child : children){
			
			if(getNext){
				
				if(isSound(child)){
					
					System.out.println("Forward Child " + child);
					
					File nextSoundFile = new File(parent.getAbsolutePath() + File.separatorChar + child);
					play(nextSoundFile);
					break;
				}
			}			
			
			if(child.equals(name)){
				getNext = true;
			}
		}
		
	}
	
	public void play(Sound s){
		
		//Pause the old sound, if it exists
		if(sound != null){
			sound.pause();
		}
		
		
		sound = s;
		sound.addSoundChangeListener(new SoundChangeListener(){

			@Override
			public void soundLoaded() {
				
				for(PlayerListener l : listenerList){
					l.soundLoaded();
				}				
				
				sound.play();
			}
			
		});
		
	}
	
	private void play(File soundFile) {
		
		sound.pause();
		
		System.out.println("New Sound Path: " + soundFile.getAbsolutePath());
		
		sound = new Sound(soundFile.getAbsolutePath());
		sound.addSoundChangeListener(new SoundChangeListener(){

			@Override
			public void soundLoaded() {
				
				for(PlayerListener l : listenerList){
					l.soundLoaded();
				}				
				
				sound.play();
			}
			
		});
	}

	public boolean isPlaying() {
		if(sound == null){
			return false;
		}
		return sound.isPlaying();
	}

	public void pause() {
		sound.pause();
	}
	
	public void play(double pos){
		sound.play(pos);
	}

	public void play() {
		sound.play();
	}

	public double getSoundPosition() {
		if(sound == null){
			return 0;
		}
		return sound.getSoundPosition();
	}
	
	public boolean hasSound() {
		return sound != null;
	}

	public Sound getSound() {
		return sound;
	}

	public void resume() {
		play(getSoundPosition());
	}
	
	public void playPause(){
		if(!isPlaying()){
			resume();
		}else{
			pause();
		}
	}

	public void setSoundPosition(double position) {
		if(sound != null){
			sound.play(position);
		}
	}
}
