# import numpy as np
# import matplotlib
# matplotlib.use('TkAgg')
# import matplotlib.pyplot as plt
#
# # 示例数据
# x = np.array([1, 3, 6, 12, 36])
# y = np.array([1000, 2000, 4000, 8000, 16000])
#
# # 多项式拟合，这里选择 2 阶多项式
# coefficients = np.polyfit(x, y, 2)  # 2 表示 2 阶多项式拟合
# polynomial = np.poly1d(coefficients)
#
# # 绘制原始数据点
# plt.scatter(x, y, color='blue', label='Data points')
#
# # 绘制拟合曲线
# x_fit = np.linspace(min(x), max(x), 100)
# y_fit = polynomial(x_fit)
# plt.plot(x_fit, y_fit, color='red', label='Polynomial fit')
#
# # 添加图例和标题
# plt.legend()
# plt.title('Polynomial Fit')
# plt.xlabel('x')
# plt.ylabel('y')
# plt.show()
#
# # 输出拟合表达式
# print(f"Polynomial fit expression: y = {coefficients[0]:.2f}x^2+{coefficients[1]:.2f}x^1 + {coefficients[2]:.2f}")

import numpy

x = numpy.array([1, 3, 6, 12,  36, 60])
y = -8.83 * x ** 2 + 755.59 * x + 1.37
print(y)
