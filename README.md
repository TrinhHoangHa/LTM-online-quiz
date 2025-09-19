<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG TRẮC NGHIỆM TRỰC TUYẾN
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="FIT DNU Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 1. Giới thiệu hệ thống📖

Hệ thống **Trắc nghiệm trực tuyến** được phát triển theo mô hình Client-Server, cho phép người dùng đăng nhập, tham gia các bài kiểm tra online, chọn đề thi và làm bài trực tiếp trên giao diện đồ họa. Kết quả làm bài sẽ được lưu trữ, hiển thị điểm số, câu đúng/sai, và thống kê điểm của từng người dùng.  

**Chức năng chính**
- Đăng nhập (lưu trữ trong file `users.csv` với định dạng username,password,score,exam_count).
- Menu chính: Chọn bài thi, bắt đầu làm bài, xem lịch sử làm bài.
- Làm bài kiểm tra: Hiển thị từng câu hỏi, các lựa chọn A/B/C/D, ghi nhận câu trả lời.
- Nộp bài: Sau khi nộp, điểm hiển thị trên cùng, các câu đúng màu xanh, câu sai màu đỏ nhạt, nút “Nộp” đổi thành “Quay lại”.
- Lịch sử làm bài: Lưu kết quả làm bài trong `exam_history.csv` (username, exam_id, score, timestamp), hiển thị thống kê tổng điểm và số bài đã làm.

## 2. Công nghệ sử dụng🔧
- Ngôn ngữ lập trình: Java (JDK 8+).
- Giao diện người dùng: Java Swing (JFrame, JButton, JLabel, JPanel, JRadioButton, JTable cho lịch sử làm bài).
- Truyền thông mạng: TCP Socket (ServerSocket cho server, Socket cho client).
- Lưu trữ dữ liệu: MySQL.
- Kiến trúc:
    - Client: `LoginFrame.java`, `RegisterFrame.java`, `ExamMenu.java`, `ExamClient.java`, `DoExamUI.java`.
    - Server: `Server.java` (quản lý đề thi, gửi câu hỏi cho client, nhận đáp án và trả kết quả).
    - Thread và I/O: xử lý đồng bộ giữa client-server, lưu/truy xuất dữ liệu SQL, gửi điểm và kết quả trả về.

## 3. Hình ảnh các chức năng🚀
- Màn hình đăng nhập
    - Nhập username/password, kiểm tra regex (username: 3-20 ký tự chữ cái/số/underscore; password: ít nhất 6 ký tự với chữ hoa/thường/số/ký tự đặc biệt).

<p align="center">
  <img src="docs/dangnhap.png" alt="Màn hình đăng nhập" width="400"/>
</p>
<p align="center">
  <em> Hình 1: Màn hình đăng nhập (LoginFrame) </em>
</p>


- Màn hình menu chính
    - Chọn đề thi, bắt đầu làm bài, xem lịch sử làm bài.

<p align="center">
  <img src="docs/menu.png" alt="Màn hình menu" width="400"/>
</p>
<p align="center">
  <em> Hình 2: Menu chính (ExamMenu) </em>
</p>

- Màn hình làm bài
    - Hiển thị câu hỏi, lựa chọn A/B/C/D, sidebar câu hỏi, nút nộp bài.

<p align="center">
  <img src="docs/lambai.png" alt="Màn hình làm bài" width="500"/>
</p>
<p align="center">
  <em> Hình 3: Giao diện làm bài kiểm tra (DoExamUI) </em>
</p>

- Sau khi nộp bài
    - Câu đúng màu xanh, câu sai màu đỏ nhạt, điểm hiển thị trên cùng, nút nộp đổi thành “Quay lại”.

<p align="center">
  <img src="docs/kq.png" alt="Kết quả" width="500"/>
</p>
<p align="center">
  <em> Hình 4: Kết quả bài làm sau khi nộp </em>
</p>

- Lịch sử làm bài
    - Hiển thị thống kê số bài đã làm, điểm trung bình, chi tiết câu trả lời từng bài.

<p align="center">
  <img src="docs/lichsu.png" alt="Lịch sử làm bài" width="500"/>
</p>
<p align="center">
  <em> Hình 5: Lịch sử làm bài (ExamHistoryFrame) </em>
</p>


## 4. Cài đặt & chạy chương trình📝
- Bước 1: Chuẩn bị môi trường
    - Cài đặt Eclipse IDE for Java Developers (hoặc phiên bản Eclipse hỗ trợ Java).
    - Cài đặt Java JDK 8+.
    - Kiểm tra JDK bằng lệnh trong terminal: `java -version`.
- Bước 2: Thiết lập dự án trong Eclipse
    - Mở Eclipse, chọn **File > New > Java Project**.
    - Đặt tên dự án (ví dụ: `OnlineQuiz`) và nhấn **Finish**.
    - Sao chép thư mục `quiz` chứa các file mã nguồn (`LoginFrame.java`, `RegisterFrame.java`, `ExamMenu.java`, `Server.java`, `ExamClient.java`, `DoExamUI.java`) vào thư mục dự án.
    - Nhấn chuột phải vào dự án trong **Package Explorer**, chọn **Refresh** để cập nhật các file.
- Bước 3: Chạy chương trình
    - Mở `LoginFrame.java` → **Run As > Java Application**.
    - Đăng nhập tài khoản.
    - Trong menu, chọn đề thi, bắt đầu làm bài, hoặc xem lịch sử làm bài.
- Bước 4: Làm bài kiểm tra
    - Chọn đáp án A/B/C/D.
    - Sau khi nộp: điểm hiển thị trên cùng, câu đúng/sai màu nền, nút nộp đổi thành “Quay lại”.

## 👜Thông tin cá nhân
**Họ tên**: Trịnh Hoàng Hà.  
**Lớp**: CNTT 16-01.  
**Email**: trinhhoangha30@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.


---
