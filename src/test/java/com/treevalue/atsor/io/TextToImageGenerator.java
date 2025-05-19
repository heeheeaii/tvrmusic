package com.treevalue.atsor.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.RenderingHints;
import java.awt.FontMetrics;

public class TextToImageGenerator {

    public static void main(String[] args) {
        // 1. 检查是否提供了输入文本
        if (args.length == 0) {
            System.err.println("错误: 请提供要生成图片的文本作为参数。");
            System.err.println("用法: java TextToImageGenerator <文本内容>");
            System.exit(1);
        }

        // 2. 将所有参数合并为一个字符串 (允许文本中包含空格)
        String text = String.join(" ", args);

        // 3. 定义图片属性
        int width = 800; // 图片宽度
        int height = 200; // 图片高度
        String outputFileName = "output.png"; // 输出图片文件名
        Font font = new Font("SansSerif", Font.BOLD, 48); // 字体样式和大小

        try {
            // 4. 创建 BufferedImage 对象
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // 5. 获取 Graphics2D 对象，用于绘制
            Graphics2D g2d = image.createGraphics();

            // 6. 开启抗锯齿，使文字更平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 7. 设置背景色为黑色并填充
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);

            // 8. 设置字体和颜色（白色）
            g2d.setFont(font);
            g2d.setColor(Color.WHITE);

            // 9. 计算文本居中所需的位置
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent(); // 使用 Ascent 获取基线以上的高度

            // 水平居中
            int x = (width - textWidth) / 2;
            // 垂直居中 (基于基线)
            // (图片高度 - 字体总高度) / 2 + 字体基线以上的高度
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();

            // 10. 在计算好的位置绘制文本
            g2d.drawString(text, x, y);

            // 11. 释放 Graphics 对象资源
            g2d.dispose();

            // 12. 将图片保存到文件
            File outputFile = new File(outputFileName);
            ImageIO.write(image, "png", outputFile);

            System.out.println("图片已成功生成: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("生成图片时出错: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("发生未知错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
