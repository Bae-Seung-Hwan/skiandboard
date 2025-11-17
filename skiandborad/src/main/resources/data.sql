INSERT INTO ski_resort (name, description, hero_image_url, lat, lng, homepage_url)
VALUES
('휘닉스 파크','초급 코스가 좋아요','/img/resort1.jpg',37.58,128.32,'https://example.com'),
('하이원','설질 최고','/img/resort2.jpg',37.20,128.82,'https://example.com');

INSERT INTO course_slot (title, time_range, note, display_order)
VALUES
('오전 초급 코스','09:00 ~ 11:00','몸풀기 추천',1),
('점심 중급 코스','12:30 ~ 14:00','완만한 슬로프',2),
('오후 상급 코스','14:30 ~ 16:00','도전 난이도',3),
('야간 라이트 런','19:00 ~ 20:30','야경 맛집',4);
