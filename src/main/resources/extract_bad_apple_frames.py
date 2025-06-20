#!/usr/bin/env python3
"""
Frame Extractor

Example:
    python (video_path.mp4) (output_directory) (fps) (max_frames(optional))

    python extract_bad_apple_frames.py bad_apple.mp4 bad_apple_frames 30
"""

import cv2
import os
import sys
import argparse

def extract_frames(video_path, output_dir, fps=30, max_frames=None):
    """
    Extract frames from a video file.
    
    Args:
        video_path (str): Path to the video file
        output_dir (str): Directory to save frames
        fps (int): Frames per second to extract
        max_frames (int): Maximum number of frames to extract (None for all)
    """
    
    # Open the video file
    cap = cv2.VideoCapture(video_path)
    
    if not cap.isOpened():
        print(f"Error: Could not open video file {video_path}")
        return False
    
    # Get video properties
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    video_fps = cap.get(cv2.CAP_PROP_FPS)
    
    print(f"Video: {video_path}")
    print(f"Total frames: {total_frames}")
    print(f"Video FPS: {video_fps}")
    print(f"Extracting at: {fps} FPS")
    
    # Calculate frame interval
    frame_interval = max(1, int(video_fps / fps))
    
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Extract frames
    frame_count = 0
    saved_count = 0
    
    while True:
        ret, frame = cap.read()
        
        if not ret:
            break
            
        # Save frame at specified interval
        if frame_count % frame_interval == 0:
            # Convert to grayscale for better performance
            gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            
            # Resize to a reasonable size for Minecraft
            resized_frame = cv2.resize(gray_frame, (64, 48))
            
            # Save frame
            frame_filename = f"frame_{saved_count:06d}.png"
            frame_path = os.path.join(output_dir, frame_filename)
            cv2.imwrite(frame_path, resized_frame)
            
            saved_count += 1
            
            # Progress update
            if saved_count % 100 == 0:
                print(f"Extracted {saved_count} frames...")
            
            # Check if we've reached max frames
            if max_frames and saved_count >= max_frames:
                break
        
        frame_count += 1
    
    cap.release()
    
    print(f"Extraction complete! Saved {saved_count} frames to {output_dir}")
    return True

def main():
    parser = argparse.ArgumentParser(description='Extract frames from video')
    parser.add_argument('video_file', help='Path to the video file')
    parser.add_argument('output_dir', nargs='?', default='frames', 
                       help='Output directory for frames (default:frames)')
    parser.add_argument('--fps', type=int, default=30, 
                       help='Frames per second to extract (default: 30)')
    parser.add_argument('--max-frames', type=int, 
                       help='Maximum number of frames to extract')
    
    args = parser.parse_args()
    
    # Check if video file exists
    if not os.path.exists(args.video_file):
        print(f"Error: Video file '{args.video_file}' not found")
        sys.exit(1)
    
    # Extract frames
    success = extract_frames(args.video_file, args.output_dir, args.fps, args.max_frames)
    
    if success:
        print("\nNext steps:")
        print("1. Copy the extracted frames to: plugins/Hologram/bad_apple_frames/")
        print("2. Restart your Minecraft server")
        print("3. Use the music disc 13 item in-game")
    else:
        sys.exit(1)

if __name__ == "__main__":
    main() 