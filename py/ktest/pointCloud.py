import numpy as np
import open3d as o3d

def generate_point_cloud(center, num_points=1000, radius=1.0):
    phi = np.random.uniform(0, np.pi, num_points)
    theta = np.random.uniform(0, 2 * np.pi, num_points)
    x = radius * np.sin(phi) * np.cos(theta) + center[0]
    y = radius * np.sin(phi) * np.sin(theta) + center[1]
    z = radius * np.cos(phi) + center[2]

    points = np.vstack((x, y, z)).T
    return points

# 生成两个点云
pc1 = generate_point_cloud(center=(0, 0, 0))
pc2 = generate_point_cloud(center=(2, 2, 2))

# 将点云数据转换为Open3D点云对象
pcd1 = o3d.geometry.PointCloud()
pcd1.points = o3d.utility.Vector3dVector(pc1)

pcd2 = o3d.geometry.PointCloud()
pcd2.points = o3d.utility.Vector3dVector(pc2)

# 可视化函数：显示点云的不同视角
def visualize_point_clouds(pcd1, pcd2, view_angle):
    # 设定视角
    vis = o3d.visualization.Visualizer()
    vis.create_window()

    # 将点云添加到可视化器
    vis.add_geometry(pcd1)
    vis.add_geometry(pcd2)

    # 设置视角
    ctr = vis.get_view_control()
    ctr.set_front(view_angle[0])
    ctr.set_up(view_angle[1])
    ctr.set_lookat(view_angle[2])
    ctr.set_zoom(view_angle[3])

    # 渲染并显示
    vis.run()
    vis.destroy_window()

# 设定三个不同的视角
view_angles = [
    ([0, 0, 1], [0, 1, 0], [0, 0, 0], 0.8),  # 正面
    ([1, 0, 0], [0, 1, 0], [0, 0, 0], 0.8),  # 侧面
    ([0, 0, -1], [0, 1, 0], [0, 0, 0], 0.8)  # 底部
]

# 可视化：逐个展示三个视角
for angle in view_angles:
    visualize_point_clouds(pcd1, pcd2, angle)

# 比较两个点云
def compare_point_clouds(pcd1, pcd2):
    # 转换为numpy数组
    points1 = np.asarray(pcd1.points)
    points2 = np.asarray(pcd2.points)

    # 计算每个点到其他点的欧氏距离
    distances = np.linalg.norm(points1[:, np.newaxis] - points2, axis=2)
    min_distances = np.min(distances, axis=1)

    # 计算平均最小距离
    avg_distance = np.mean(min_distances)
    print(f"Average minimum distance between the two point clouds: {avg_distance:.4f}")

# 比较两个点云
compare_point_clouds(pcd1, pcd2)
