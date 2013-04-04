package com.ptune.audio;

import android.util.Log;

/**
 * Contains a frequency-domain representation of a sampled input
 * signal. Stores the spectrum and provides methods for accessing
 * and manipulating the spectrum.
 * 
 * @author Weston Pay
 *
 */
public class Spectrum {

	double[] spectrum;
	double[] samples;
	int sampleRate;

	/**
	 * Creates a spectrum from the provided byte data and given sample rate.
	 * 
	 * @param data Non-interlaced array of sample data, such as that retrieved
	 * from an audio stream.
	 * @param sampleRateInHz Sample rate used to record sample data.
	 */
	public Spectrum(byte[] data, int sampleRateInHz) {
		this.sampleRate = sampleRateInHz;
		buildSpectrum(data);
	}
	
	public double[] getSpectrum() {
		return spectrum.clone();
	}
	
	public double[] getSamples() {
		return samples.clone();
	}

	/**
	 * Builds the frequency-domain spectrum from sampled audio data.
	 * 
	 * @param data Non-interlaced byte array of sample data.
	 */
	public void buildSpectrum(byte[] data) {
		// Create interlaced double array of complex numbers to hold sample data.
		samples = byteToDouble(data);
		
		// Apply window function to sample data.
		hanningWindow(samples);

		// Build FFT.
		FFT fft = new FFT(samples.length/2,-1);
		fft.transform(samples);

		// Build frequency spectrum from interlaced results data.
		spectrum = new double[samples.length/2];
		for (int i = 0; i < samples.length; i+=2)
			spectrum[i/2] = Math.sqrt(samples[i] * samples[i]+ samples[i+1] * samples[i+1]);
	}

	/**
	 * Converts byte array sample data into an interlaced array of doubles.
	 * The double alternates real and imaginary values, imaginary values are
	 * set to zero in this method.
	 * 
	 * @param data Non-interlaced byte array of sample data.
	 * @return Interlaced double array of sample data normalized to the
	 * range [-1.0 .. +1.0].
	 */
	private double[] byteToDouble(byte[] data) {
		double[] sample = new double[data.length];

		
		// Converts 16-bit big endian byte data into an interlaced array of doubles.
//		for (int i = 0; i < data.length; i+=2)
//			sample[i] = ((data[i] << 8) | (data[i+1] & 0xFF)) / 32768.0;

		// Converts 16-bit little endian byte data into an interlaced array of doubles.
		for (int i = 0; i < data.length; i+=2)
			sample[i] = ((data[i] & 0xFF) | (data[i+1] << 8)) / 32768.0;

		return sample;
	}
	
	/**
	 * Applies a Hanning window to the supplied sample data.
	 * 
	 * @param samples Interlaced array of sample data.
	 */
	public static void hanningWindow(double[] samples) {
		for (int i = 0; i < samples.length; i+=2) {
			double hanning = 0.5 - 0.5 * Math.cos((2*Math.PI*(i/2)) / (samples.length/2) - 1);
			samples[i] *= hanning;
		}
	}

	/**
	 * Calculates the fundamental frequency of the spectrum, the frequency
	 * with the highest magnitude.
	 * 
	 * @return Float value that represents the frequency. Float
	 * accuracy is generally all that is needed for typical purposes.
	 */
	public float getFrequency() {
		float frequency = 0;
		float peak = 0;
		
		// Determine the average magnitude of the spectrum peak values
		double average = 0;
		double counter = 0;
		// search only the first half.
		for (int i = 1; i < spectrum.length/2; i++) {
			if (spectrum[i] > 1){
				if (spectrum[i] > spectrum[i-1] && spectrum[i] > spectrum[i+1]) { // Is it a peak?
					average += spectrum[i];
					counter++;
					//Log.d("Spectrum", "Value: " + Double.toString(spectrum[i]));
				}
			}
		}
		average /= counter;
		
		//Log.d("Spectrum", "Average: " + Double.toString(average));
		
		// Find the index with highest magnitude,
		int max = 0;
		double largest = 1.0;
		// search only the first half.
		for (int i = 0; i < spectrum.length/2; i++) {
			if (spectrum[i] > largest) {
				largest = spectrum[i];
				max = i;
			}
		}
		
		//Log.d("Spectrum", "Largest: " + Double.toString(largest));
		
		// Calculate the quadratic interpolation peak index.
		if (max > 0) {
			peak = quadraticPeak(max);
		}

		/* 
		 * Calculate the frequency of the highest peak in the range if it is
		 * greater than the average overall peak values.
		 */
		if (largest > average*5) {
			// Frequency = Fs * i / N
			float freqFraction = peak / (float)spectrum.length; // The fraction of the sampling rate.
			frequency = (float) sampleRate * freqFraction;
		}
		return frequency;
	}
	
	/**
	 * Calculates the fundamental frequency of the spectrum within half
	 * the target frequency to twice the target frequency if it is within
	 * a significant amplitude of the highest amplitude peak.
	 * 
	 * @return Float value that represents the frequency. Float
	 * accuracy is generally all that is needed for typical purposes.
	 */
	public float getFrequency(float target) {
		float frequency = 0, peak = 0;
		
		// Calculate lower and upper range index values. Lower = 1/2 target, upper = target * 2.
		int lower = Math.round((target / 2) * spectrum.length / sampleRate) + 1;
		int upper = (int)((target * 2) * spectrum.length / sampleRate) - 1;
		
		// If the upper value is greater than the Nyquist limit set it to equal the limit.
		if (upper > (spectrum.length / 2)) {
			upper = (spectrum.length / 2);
		}
		
		// Determine the average magnitude of the spectrum peak values
		double average = 0;
		// search only the first half.
		for (int i = 1; i < spectrum.length/2; i++) {
			if (spectrum[i] > spectrum[i-1] && spectrum[i] > spectrum[i+1]) { // Is it a peak?
					average += spectrum[i];
			}
		}
		average /= spectrum.length/2;
		
		// Find the peak with highest magnitude within the given range.
		int max = -1;
		//largest *= 0.9; // A peak is not valid unless it is within a certain percentage.
		double largestInRange = 0;
		for (int i = lower; i < upper; i++) {
			if (spectrum[i] > spectrum[i-1] && spectrum[i] > spectrum[i+1]) { // Is it a peak?
				if (spectrum[i] > largestInRange) {
					largestInRange = spectrum[i];
					max = i;
				}
			}
		}
		
		// Calculate the quadratic interpolation peak index.
		if (max > 0) {
			peak = quadraticPeak(max);
		}

		/* 
		 * Calculate the frequency of the highest peak in the range if it is
		 * greater than the average overall peak values.
		 */
		if (largestInRange > average*2) {
			// Frequency = Fs * i / N
			float freqFraction = peak / (float)spectrum.length; // The fraction of the sampling rate.
			frequency = (float) sampleRate * freqFraction;
		}
		return frequency;
	}
	
	/**
	 * Quadratic Interpolation of Peak Location
	 * 
	 * <p>Provides a more accurate value for the peak based on the
	 * best fit parabolic function.
	 * 
	 * <p>α = spectrum[max-1]
	 * <br>β = spectrum[max]
	 * <br>γ = spectrum[max+1]
	 * 
	 * <p>p = 0.5[(α - γ) / (α - 2β + γ)] = peak offset
	 * 
	 * <p>k = max + p = interpolated peak location
	 * 
	 * <p>Courtesy: <a href="https://ccrma.stanford.edu/~jos/sasp/Quadratic_Interpolation_Spectral_Peaks.html">
	 * information source</a>.
	 *
	 * @param index The estimated peak value to base a quadratic interpolation on.
	 * @return Float value that represents a more accurate peak index in a spectrum.
	 */
	private float quadraticPeak(int index) {
		float alpha, beta, gamma, p, k;
		
		alpha = (float)spectrum[index-1];
		beta = (float)spectrum[index];
		gamma = (float)spectrum[index+1];
		
		p = 0.5f * ((alpha - gamma) / (alpha - 2*beta + gamma));
		
		k = (float)index + p;
		
		return k;
	}

	/**
	 * Prints the spectrum values to the console, one value per line.
	 */
	public void printSpectrum() {
		for (double d : spectrum) {
			System.out.println(d);
		}
	}
}
