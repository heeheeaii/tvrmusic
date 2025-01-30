import cv2
import numpy as np

width, height = 640.0, 480.0
fps = 30
duration = 10
total_frames = fps * duration

ball_radius = 1
ball_color = (0, 255, 0)
ball_position = np.array([width / 4, height / 2])
ball_velocity = np.array([5.0, -5.0])

square_size = 1
square_color = (0, 0, 255)
square_position = np.array([width / 2, height / 2])
square_velocity = np.array([-3.0, 4.0])

fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter(R'D:\agi\tvrmusicnew\py\ktest\bouncing_ball_and_square.avi', fourcc, fps,
                      (int(width), int(height)))

for frame_idx in range(total_frames):
    frame = np.zeros((int(height), int(width), 3), dtype=np.uint8)

    ball_position += ball_velocity
    if ball_position[0] - ball_radius < 0 or ball_position[0] + ball_radius > width:
        ball_velocity[0] = -ball_velocity[0]
    if ball_position[1] - ball_radius < 0 or ball_position[1] + ball_radius > height:
        ball_velocity[1] = -ball_velocity[1]

    square_position += square_velocity
    if square_position[0] < 0 or square_position[0] + square_size > width:
        square_velocity[0] = -square_velocity[0]
    if square_position[1] < 0 or square_position[1] + square_size > height:
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
