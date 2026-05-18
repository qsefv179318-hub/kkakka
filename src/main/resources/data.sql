INSERT INTO snack (id, name, image_url, manufacturer, average_rating) VALUES
(1, '포카칩 오리지널',   'https://example.com/img/pocachip.png',   '오리온', 4.5),
(2, '초코파이',          'https://example.com/img/chocopie.png',   '오리온', 4.7),
(3, '꼬북칩 콘스프맛',   'https://example.com/img/kkobukchip.png', '오리온', 4.6),
(4, '신짱구 매운맛',     'https://example.com/img/sinjjang.png',   '농심',   4.2),
(5, '카스타드',          'https://example.com/img/custard.png',    '롯데',   4.4)
ON CONFLICT (id) DO NOTHING;

INSERT INTO snack_analysis
(snack_id, sweet_score, salty_score, spicy_score, crispy_score, soft_score,
 positive_ratio, negative_ratio, top_keywords, updated_at) VALUES
(1, 2.0, 8.5, 0.0, 9.7, 1.5, 88, 12, '바삭함,짭짤함,감자맛',      NOW()),
(2, 9.2, 1.0, 0.0, 2.0, 8.8, 92,  8, '달콤함,부드러움,마시멜로',  NOW()),
(3, 4.0, 6.0, 0.0, 9.5, 2.0, 90, 10, '바삭함,콘스프,중독성',      NOW()),
(4, 1.0, 5.0, 9.3, 7.5, 1.0, 75, 25, '매운맛,자극적,청양고추',    NOW()),
(5, 7.5, 1.5, 0.0, 1.5, 9.5, 89, 11, '부드러움,촉촉함,우유와함께',NOW())
ON CONFLICT (snack_id) DO NOTHING;

-- ID Auto Increment 시퀀스 보정 (수동 ID INSERT 후 필수)
SELECT setval('snack_id_seq', (SELECT MAX(id) FROM snack));
