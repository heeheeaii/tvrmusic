import zipfile
import os

# 解压缩文件
zip_file_path = 'vangogh2photo.zip'
output_dir = 'vangogh2photo'

with zipfile.ZipFile(zip_file_path, 'r') as zip_ref:
    zip_ref.extractall(output_dir)
