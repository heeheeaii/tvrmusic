import cv2
import os
import numpy as np


class VideoGenerator:
    def __init__(self, width=640.0, height=480.0, fps=30, duration=10):
        self.__width, self.__height = width, height
        self.__fps = fps
        self.__duration = duration
        self.__total_frames = fps * duration

    def generate_simple_video(self, outPath=R'D:\agi\tvrmusicnew\py\ktest\bouncing_ball_and_square.avi'):
        ball_radius = 1
        ball_color = (0, 255, 0)
        ball_position = np.array([self.__width / 4, self.__height / 2])
        ball_velocity = np.array([5.0, -5.0])

        square_size = 1
        square_color = (0, 0, 255)
        square_position = np.array([self.__width / 2, self.__height / 2])
        square_velocity = np.array([-3.0, 4.0])

        fourcc = cv2.VideoWriter_fourcc(*'XVID')
        out = cv2.VideoWriter(outPath, fourcc, self.__fps,
                              (int(self.__width), int(self.__height)))

        for frame_idx in range(self.__total_frames):
            frame = np.zeros((int(self.__height), int(self.__width), 3), dtype=np.uint8)

            ball_position += ball_velocity
            if ball_position[0] - ball_radius < 0 or ball_position[0] + ball_radius > self.__width:
                ball_velocity[0] = -ball_velocity[0]
            if ball_position[1] - ball_radius < 0 or ball_position[1] + ball_radius > self.__height:
                ball_velocity[1] = -ball_velocity[1]

            square_position += square_velocity
            if square_position[0] < 0 or square_position[0] + square_size > self.__width:
                square_velocity[0] = -square_velocity[0]
            if square_position[1] < 0 or square_position[1] + square_size > self.__height:
                square_velocity[1] = -square_velocity[1]

            ball_position_int = ball_position.astype(int)

            cv2.circle(frame, tuple(ball_position_int), ball_radius, ball_color, 5)
            cv2.rectangle(frame, square_position.astype(int),
                          (int(square_position[0] + square_size), int(square_position[1] + square_size)),
                          square_color, 5)

            out.write(frame)

            if np.linalg.norm(ball_velocity) > 0.1:
                ball_velocity *= 0.99
            if np.linalg.norm(square_velocity) > 0.1:
                square_velocity *= 0.99

        out.release()
        print("视频已生成：bouncing_ball_and_square.avi")

    def video_to_frames(self, video_path=R'D:\agi\tvrmusicnew\py\ktest\bouncing_ball_and_square.avi',
                        output_folder=R'D:\agi\tvrmusicnew\py\ktest\images'):
        if not os.path.exists(output_folder):
            os.makedirs(output_folder)

        cap = cv2.VideoCapture(video_path)

        if not cap.isOpened():
            print("Error: Couldn't open video.")
            exit()

        frame_count = 0

        while True:
            ret, frame = cap.read()
            if ret:
                frame_filename = os.path.join(output_folder, f"out_{frame_count:04d}.jpg")
                cv2.imwrite(frame_filename, frame)
                frame_count += 1
            else:
                break
        cap.release()
        print(f"Total {frame_count} frames saved.")


if __name__ == '__main__':
    v = VideoGenerator()
    v.generate_simple_video()
    v.video_to_frames()
