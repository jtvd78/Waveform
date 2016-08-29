package com.hoosteen.waveform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Sound {
	
	//Sampling rate of the sound
	float sampleRate;
	
	//Is only true when the sound is finished loading
	boolean finished = false;
	
	/*The thing that plays the sound*/
	Clip clip;
	
	//Path of the sound being played
	String soundPath;
	
	int[] leftFrameArray;
	int[] rightFrameArray;
	
	String filepath;
	
	/**
	 * Constructs a Sound object
	 * @param filepath The file path of the sound. May only be a .wav or an .mp3
	 */
	public Sound(String filepath){
		
		System.out.println("Set Sound Filepath " + filepath);
		
		setFile(filepath);
		
		this.filepath = filepath;
		
		//Read the file in another thread
		new Thread(new Runnable(){

			@Override
			public void run() {
				
				readFile();
				finishedReading();
				
			}
		}).start();	
		
	}
	
	public String getFilepath(){
		return filepath;
	}
	
	private void finishedReading(){
		for(SoundChangeListener listener : listenerList){
			listener.soundLoaded();
		}
	}
	
	public void addSoundChangeListener(SoundChangeListener listener){
		listenerList.add(listener);
	}
	
	ArrayList<SoundChangeListener> listenerList = new ArrayList<SoundChangeListener>();
	
	/**
	 * Gets the amount of frames in the Sound
	 * Left and Right channels will always have the same length
	 * @return the size (in frames) of the left channel
	 */
	public int getFrameCount(){
		
		if(!isFinished()){
			return 0;
		}
		
		//Same as leftFrameArray's length
		return leftFrameArray.length;
	}
	
	/**
	 * Sets the file of the sound. Can either be a wav or an mp3
	 * @param path The path to the specified file
	 */
	private void setFile(String path){
		
		//TODO Make this work generically for all people
		
		if(path.endsWith(".mp3")){
			//Sound is am mp3, extract it into a wav
			
			//Path for the extracted .wav
			String newPath = "D:\\justin\\Temp\\WaveformSongCache\\" + path.substring(path.lastIndexOf('\\') + 1, path.lastIndexOf('.')) + ".wav";
			
			//Skip if the extracted song already exists. 
			if(!new File(newPath).exists()){
				
				//Makes sure the parent folder is there
				new File("D:\\justin\\Temp\\WaveformSongCache\\").mkdirs();
				
				//Convert the song, place it in the newPath directory
				Converter c = new Converter();
				try {
					c.convert(path, newPath);
				} catch (JavaLayerException e) {
					e.printStackTrace();
				}
			}	
			
			//Set the path of the sound
			soundPath = newPath;
		}else if(path.endsWith(".wav")){
			
			//Set the path of the sound
			soundPath = path;
		}
	}
	
	/**
	 * Reads the file at soundPath into memory
	 */
	private void readFile(){
		try {
			
			//File of the song (must be a .wav)
			File file = new File(soundPath);
			
			//Load the data into memory
			byte[] data = Files.readAllBytes(file.toPath());
			
			//Init the audio input stream, and save the sample rate of the sound
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			sampleRate = ais.getFormat().getSampleRate();
			
			//Frame size. This code won't really work if frameSize
			//is any number other than 4. But, that is the type of
			//sound data I'm using, so the code is going to stay this way.
			int frameSize = ais.getFormat().getFrameSize();
			
			//The number of frames in the sound
			int frames = data.length/frameSize;
			
			//Init the array with the length as the number of total frames
			
			leftFrameArray = new int[frames];
			rightFrameArray = new int[frames];
			
			//Loop through the data
			for(int i = 0; i < frames ; i+=1){
				
				//Extract the left and right channels from the data
				leftFrameArray[i] =  ((int)data[frameSize*i + 1] << 8) | ((int)data[frameSize*i + 0] & 0xFF);
				rightFrameArray[i] = ((int)data[frameSize*i + 3] << 8) | ((int)data[frameSize*i + 2] & 0xFF);
			}		
			
			//Close input stream, set finished to true
			ais.close();
			finished = true;
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		
		//Display in console if song was loaded successfully
		if(finished){
			System.out.println("Sound Successfully Loaded");
		}else{
			System.out.println("Sound Loading Failed");
		}
	}

	/**
	 * Gets a specific section of the sound's data
	 * @param startPos (0.0 to 1.0) The starting position of the section
	 * @param endPos (0.0 to 1.0) The ending position of the section
	 * @param width The number of frames to add to the result
	 * @return a section of the sound, ranging from start to end with a given width
	 */
	public int[][] getSection(float startPos, float endPos, int width) {		
		
		//Gets the size of the sound
		int size = getFrameCount();
		
		//Stores the output of the method
		int[][] output = new int[width][2];
		
		//Integer locations within the sound's data where the section lies
		int frameStart = (int) (size*startPos);
		int frameEnd = (int) (size*endPos);
		
		//Protection against Out of Bounds Exceptions
		if(frameStart < 0){
			frameStart = 0;
		}
		
		if(frameEnd >= getFrameCount()){
			frameEnd = getFrameCount()-1;
		}
		
		//Spacing between the frames to get
		float spacing = ((float)(frameEnd - frameStart))/((float)(width));
		
		//Get spaced frames for each pixel
		for(int i = 0; i < width; i++){	
			
			output[i][0] = leftFrameArray[(int) (i*spacing + frameStart)];		//left
			output[i][1] = rightFrameArray[(int) (i*spacing + frameStart)];	      //right
			
		}
		
		return output;
	}
	
	public double[][] getFFTData(double songPosition, int width){

		//Gets the size of the sound
		int size = getFrameCount();		
		
		double startPos = songPosition - ( (double)width / size ) / 2;
		double endPos = songPosition + ( (double)width / size ) / 2;		
		
		//Stores the output of the method
		double[][] output = new double[2][width];
		
		//Integer locations within the sound's data where the section lies
		int frameStart = (int) (size*startPos);
		int frameEnd = (int) (size*endPos);
		
		//Protection against Out of Bounds Exceptions
		if(frameStart < 0){
			frameStart = 0;
		}
		
		if(frameEnd >= getFrameCount()){
			frameEnd = getFrameCount()-1;
		}
		
		//Spacing between the frames to get
		//TODO Look at this. What's with the (width - 1) instead of just width
		float spacing = ((float)(frameEnd - frameStart))/((float)(width-1));
		
		
		//Get spaced frames for each pixel
		for(int i = 0; i < width; i++){			
			
			output[0][i] = leftFrameArray[(int) (i*spacing + frameStart)];		//left
			output[1][i] = rightFrameArray[(int) (i*spacing + frameStart)];      //right
			
		}
		
		return output;
	}
	
	public int[][] getSectionMinMax(float startPos, float endPos, int width) {		
		
		//Gets the size of the sound
		int size = getFrameCount();
		
		//Stores the output of the method
		int[][] output = new int[width][4];
		
		//Integer locations within the sound's data where the section lies
		int frameStart = (int) (size*startPos);
		int frameEnd = (int) (size*endPos);
		
		//Protection against Out of Bounds Exceptions
		if(frameStart < 0){
			frameStart = 0;
		}
		
		if(frameEnd >= getFrameCount()){
			frameEnd = getFrameCount()-1;
		}
		
		
		float spacing = ((float)(frameEnd - frameStart))/((float)(width));
		
		
		
		int leftMin;
		int leftMax;
		int rightMin;
		int rightMax;
		
		//Get spaced frames for each pixel
		for(int i = 0; i < width; i++){	
			
			int start = (int) (i*spacing + frameStart);
			int end = (int) ((i+1)*spacing + frameStart);		
			
			if(start == end){
				leftMin = leftMax = leftFrameArray[start];
				rightMin = rightMax = rightFrameArray[start];
			}else{
				
				leftMin = Integer.MAX_VALUE;
				leftMax = Integer.MIN_VALUE;
				rightMin = Integer.MAX_VALUE;
				rightMax = Integer.MIN_VALUE;
				
				for(int j = start; j < end; j++){
					int left = leftFrameArray[j];
					int right = rightFrameArray[j];
					
					if(left > leftMax){
						leftMax = left;
					}
					
					if(left < leftMin){
						leftMin = left;
					}
					
					if(right > rightMax){
						rightMax = right;
					}
					
					if(right < rightMin){
						rightMin = right;
					}
				}
			}			
			
			output[i][0] = leftMin;
			output[i][1] = leftMax;
			output[i][2] = rightMin;
			output[i][3] = rightMax;
			
		}
		
		return output;
	}
	
	public double[][] getSectionDouble(float startPos, float endPos, int width) {		
		//Gets the size of the sound
		int size = getFrameCount();
		
		//Stores the output of the method
		double[][] output = new double[2][width];
		
		//Integer locations within the sound's data where the section lies
		int frameStart = (int) (size*startPos);
		int frameEnd = (int) (size*endPos);
		
		//Protection against Out of Bounds Exceptions
		if(frameStart < 0){
			frameStart = 0;
		}
		
		if(frameEnd >= getFrameCount()){
			frameEnd = getFrameCount()-1;
		}
		
		//Spacing between the frames to get
		//TODO Look at this. What's with the (width - 1) instead of just width
		float spacing = ((float)(frameEnd - frameStart))/((float)(width-1));
		
		//Get spaced frames for each pixel
		for(int i = 0; i < width; i++){			
			
			output[0][i] = leftFrameArray[(int) (i*spacing + frameStart)];		//left
			output[1][i] = rightFrameArray[(int) (i*spacing + frameStart)];      //right
			
		}
		
		return output;
	}	
	
	
	/**
	 * Returns the sampling rate of the sound
	 * @return The sampling rate of the sound
	 */
	public float getSamplingRate(){
		return sampleRate;
	}
	
	/**
	 * Gets the number of the frame at a given position within the sound
	 * @param songPos A float from 0.0 to 1.0
	 * @return The number of the frame
	 */
	public int getFrameNumber(float songPos){
		return (int) (getFrameCount()*songPos);
	}
	
	/**
	 * Returns whether or not the Sound is finished loading into memory
	 * @return Whether or not the Sound is finished loading into memory
	 */
	public boolean isFinished(){
		return finished;
	}
	
	/**
	 * Begins playing at the beginning of the sound
	 * Calls play(0.0)
	 */
	public void play(){
		play(0.0f);
	}
	
	/**
	 * Begins playing the sound at the specified location
	 * Does nothing if the sound is not finished loading 
	 * 
	 * @param pos Where in the song to begin playing (float from 0.0 to 1.0)
	 */
	public void play(double pos){
		
		//Don't do anything if the song hasn't been loaded
		if(!finished){
			return;
		}
		
		//If the sound hasn't been played before, load the clip
		if(clip == null){
			try {
				
				//Load the audio input stream
				AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundPath));
				
				//Get the clip and open the input stream
				clip = AudioSystem.getClip();
				clip.open(ais);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Set the clip to the specified position
		clip.setFramePosition((int) (pos*getFrameCount()));
		
		//Begin playing the sound
		clip.start();
	}
	
	/**
	 * Pauses the sound
	 * Does nothing if the song has not been played yet
	 */
	public void pause(){
		if(clip == null){
			return;
		}
		
		clip.stop();
	}
	
	/**
	 * Returns whether or not the sound is playing
	 * @return Whether or not the sound is playing
	 */
	public boolean isPlaying(){
		if(clip == null){
			return false;
		}
		
		return clip.isRunning();
	}
	
	/**
	 * Gets the position of the sound
	 * @return A float from 0.0 to 1.0
	 */
	public float getSoundPosition(){
		if(clip == null){
			return 0.0f;
		}
		
		return ((float)clip.getFramePosition())/((float)getFrameCount());
	}

	public void stop() {
		if(clip != null){
			clip.stop();
		}		
	}
}