-- Development seed for husbcore.
SET search_path TO public;

INSERT INTO public.players (firstname, lastname, id) VALUES ('John', 'Doe', '3b0fbccc-ec60-48b5-a3bd-ce8cac6a6648');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Jessica', 'Doe', 'd45f473f-e181-45c3-b77e-9a7e0c0b6269');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Paul', 'Lefevre', 'fad5ee5d-c661-414e-9e22-dda0971ead5f');
INSERT INTO public.players (firstname, lastname, id) VALUES ('ZinÃ©dine', 'Zidane', 'cf41cf64-b1f2-4470-aeed-e26752ee069e');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Nicolas', 'Sarkozy', 'c978e25f-4d00-4e3c-b4d1-1b766502b7fd');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Julien', 'DorÃ©', '4aa443fd-377c-4293-b8cb-95f6dc1e7bde');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Cyril', 'Gane', '18f9b369-b3ee-46fa-9230-217fc84f6c53');
INSERT INTO public.players (firstname, lastname, id) VALUES ('AurÃ©lien', 'TchouamÃ©ni', '2e580d80-edc2-42e3-9f9f-033ea588fe83');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Niki', 'Lauda', '8cd67c05-8f38-481f-b433-2f49fa36345e');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Corentin', 'Tolisso', '56fffbd3-5bae-41eb-9550-8bff68eb5a82');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Mathilde', 'Saigner', '26cc5d1d-04e9-4655-870e-4f3b2efb559a');
INSERT INTO public.players (firstname, lastname, id) VALUES ('ValÃ©rie', 'Damidot', '8fe3b213-44f5-4a9b-8925-b3913ed7eb42');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Alexandre', 'Lacazette', '5f7dc630-cacc-4193-a04f-8f5c6ca1a204');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Adrien', 'Rabiot', 'aa99dbae-9dba-4783-ae9e-7907d60f24a1');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Corentin', 'Odapre', '1c2a9e0f-894c-484c-88f7-31cc5f0efdb2');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Philippe', 'Asdepik', '09f4fbd5-6365-4917-8c6a-c77d42e17dd1');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Lewis', 'Hamilton', '962948a8-608e-4854-bacb-38bce5b51811');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Anna', 'de Saint IrÃ©nÃ©e', '71acab85-232e-4859-906a-f5dccbe6dbc3');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Lou', 'de Saint-Croix', '96a211f7-9836-4700-a57f-e8c19bf18e92');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Hannah', 'Hadolsky', 'd56c5d66-acec-436d-9f96-062f724221a6');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Julia', 'Gelato', '70ecd6f1-f150-43cb-9c1d-bc3cfe8e6251');
INSERT INTO public.players (firstname, lastname, id) VALUES ('John', 'Cena', '916a99ad-6885-404a-b7a3-4dc20033e241');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Marie', 'BlachÃ¨re', '32a3a55d-40b9-4b50-b2c5-ff736445398c');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Admin', 'AdminLastName', '76aed36d-f1cd-42e0-bb82-ad6bb80c4741');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Lahad', 'Miqloo', '3afba91f-350b-4caf-b836-f77bb71791f1');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Flash', 'McQueen', 'a90a58fd-27f4-4812-b28b-d1cd547945ce');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Adiboo', 'PadidÃ©', 'ab2acc96-e7b4-45d9-9109-06b23eb59ae2');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Elise', 'Belleplace', '32392a37-abc1-4cbb-b8e5-79cbf13f120c');
INSERT INTO public.players (firstname, lastname, id) VALUES ('Colin', 'Dalaska', '252ae75e-2516-423a-be5d-4103cac502ba');

--




--

INSERT INTO public.player_kicker_informations
VALUES ('3afba91f-350b-4caf-b836-f77bb71791f1', 1465, 1465, 79);
INSERT INTO public.player_kicker_informations
VALUES ('ab2acc96-e7b4-45d9-9109-06b23eb59ae2', 1478, 1478, 81);
INSERT INTO public.player_kicker_informations
VALUES ('8cd67c05-8f38-481f-b433-2f49fa36345e', 1476, 1493, 253);
INSERT INTO public.player_kicker_informations
VALUES ('fad5ee5d-c661-414e-9e22-dda0971ead5f', 1425, 1484, 824);
INSERT INTO public.player_kicker_informations
VALUES ('1c2a9e0f-894c-484c-88f7-31cc5f0efdb2', 1486, 1495, 192);
INSERT INTO public.player_kicker_informations
VALUES ('32a3a55d-40b9-4b50-b2c5-ff736445398c', 1496, 1497, 37);
INSERT INTO public.player_kicker_informations
VALUES ('2e580d80-edc2-42e3-9f9f-033ea588fe83', 1531, 1553, 430);
INSERT INTO public.player_kicker_informations
VALUES ('5f7dc630-cacc-4193-a04f-8f5c6ca1a204', 1513, 1496, 80);
INSERT INTO public.player_kicker_informations
VALUES ('96a211f7-9836-4700-a57f-e8c19bf18e92', 1512, 1512, 50);
INSERT INTO public.player_kicker_informations
VALUES ('a90a58fd-27f4-4812-b28b-d1cd547945ce', 1466, 1482, 95);
INSERT INTO public.player_kicker_informations
VALUES ('70ecd6f1-f150-43cb-9c1d-bc3cfe8e6251', 1529, 1556, 306);
INSERT INTO public.player_kicker_informations
VALUES ('d45f473f-e181-45c3-b77e-9a7e0c0b6269', 1302, 1400, 1000);
INSERT INTO public.player_kicker_informations
VALUES ('cf41cf64-b1f2-4470-aeed-e26752ee069e', 1837, 1551, 3936);
INSERT INTO public.player_kicker_informations
VALUES ('71acab85-232e-4859-906a-f5dccbe6dbc3', 1306, 1480, 3621);
INSERT INTO public.player_kicker_informations
VALUES ('26cc5d1d-04e9-4655-870e-4f3b2efb559a', 1341, 1397, 2539);
INSERT INTO public.player_kicker_informations
VALUES ('4aa443fd-377c-4293-b8cb-95f6dc1e7bde', 1423, 1468, 4392);
INSERT INTO public.player_kicker_informations
VALUES ('aa99dbae-9dba-4783-ae9e-7907d60f24a1', 1985, 1721, 1612);
INSERT INTO public.player_kicker_informations
VALUES ('962948a8-608e-4854-bacb-38bce5b51811', 2147, 1933, 4156);
INSERT INTO public.player_kicker_informations
VALUES ('8fe3b213-44f5-4a9b-8925-b3913ed7eb42', 981, 1206, 2653);
INSERT INTO public.player_kicker_informations
VALUES ('c978e25f-4d00-4e3c-b4d1-1b766502b7fd', 1617, 1490, 704);
INSERT INTO public.player_kicker_informations
VALUES ('56fffbd3-5bae-41eb-9550-8bff68eb5a82', 1445, 1458, 280);
INSERT INTO public.player_kicker_informations
VALUES ('3b0fbccc-ec60-48b5-a3bd-ce8cac6a6648', 1308, 1393, 837);
INSERT INTO public.player_kicker_informations
VALUES ('09f4fbd5-6365-4917-8c6a-c77d42e17dd1', 1309, 1428, 2158);
INSERT INTO public.player_kicker_informations
VALUES ('76aed36d-f1cd-42e0-bb82-ad6bb80c4741', 1552, 1611, 4300);
INSERT INTO public.player_kicker_informations
VALUES ('d56c5d66-acec-436d-9f96-062f724221a6', 1772, 1588, 4290);
INSERT INTO public.player_kicker_informations
VALUES ('916a99ad-6885-404a-b7a3-4dc20033e241', 1253, 1300, 539);
INSERT INTO public.player_kicker_informations
VALUES ('18f9b369-b3ee-46fa-9230-217fc84f6c53', 1545, 1513, 3531);

--

INSERT INTO public.teams
VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'KickerTeam', 'Kicker', true, false);


--

INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '3afba91f-350b-4caf-b836-f77bb71791f1');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'cf41cf64-b1f2-4470-aeed-e26752ee069e');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '09f4fbd5-6365-4917-8c6a-c77d42e17dd1');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '2e580d80-edc2-42e3-9f9f-033ea588fe83');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '962948a8-608e-4854-bacb-38bce5b51811');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'aa99dbae-9dba-4783-ae9e-7907d60f24a1');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '4aa443fd-377c-4293-b8cb-95f6dc1e7bde');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'd45f473f-e181-45c3-b77e-9a7e0c0b6269');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '8cd67c05-8f38-481f-b433-2f49fa36345e');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'a90a58fd-27f4-4812-b28b-d1cd547945ce');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'ab2acc96-e7b4-45d9-9109-06b23eb59ae2');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '1c2a9e0f-894c-484c-88f7-31cc5f0efdb2');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '70ecd6f1-f150-43cb-9c1d-bc3cfe8e6251');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '96a211f7-9836-4700-a57f-e8c19bf18e92');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'fad5ee5d-c661-414e-9e22-dda0971ead5f');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '56fffbd3-5bae-41eb-9550-8bff68eb5a82');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '71acab85-232e-4859-906a-f5dccbe6dbc3');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '5f7dc630-cacc-4193-a04f-8f5c6ca1a204');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'd56c5d66-acec-436d-9f96-062f724221a6');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', 'c978e25f-4d00-4e3c-b4d1-1b766502b7fd');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '26cc5d1d-04e9-4655-870e-4f3b2efb559a');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '8fe3b213-44f5-4a9b-8925-b3913ed7eb42');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '18f9b369-b3ee-46fa-9230-217fc84f6c53');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '76aed36d-f1cd-42e0-bb82-ad6bb80c4741');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '3b0fbccc-ec60-48b5-a3bd-ce8cac6a6648');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '916a99ad-6885-404a-b7a3-4dc20033e241');
INSERT INTO public.player_team (team_id, player_id) VALUES ('c0064571-1ab7-41d3-8199-ec61a3a7d460', '32a3a55d-40b9-4b50-b2c5-ff736445398c');

--

INSERT INTO public.users (email, players_id, id, identity_user_id, role)
VALUES ('admin@gmail.com', '76aed36d-f1cd-42e0-bb82-ad6bb80c4741', 'f027e5f9-31a1-42ac-90b3-dc437585b839',
        'f027e5f9-31a1-42ac-90b3-dc437585b839', 'ADMIN');
INSERT INTO public.users (email, players_id, id, identity_user_id, role)
VALUES ('user@gmail.com', NULL, 'dc437585-98ea-4efe-90b3-b83f027e5f99', 'dc437585-98ea-4efe-90b3-b83f027e5f99', 'USER');

--

INSERT INTO public.kicker_match_access_codes (id, code, status, created_at, expires_at, last_used_at, usage_count,
                                              revoked_at, revoked_reason, created_by)
VALUES ('d0ab5fcb-2810-47c6-8f9a-2b9e7ccf4a65', 'PUBLIC', 'ACTIVE', CURRENT_TIMESTAMP, NULL, NULL, 0, NULL, NULL,
        'f027e5f9-31a1-42ac-90b3-dc437585b839');
INSERT INTO public.kicker_match_access_codes (id, code, status, created_at, expires_at, last_used_at, usage_count,
                                              revoked_at, revoked_reason, created_by)
VALUES ('c5be2878-325e-4d0d-852e-24cf0614df4a', 'DISABLED', 'REVOKED', CURRENT_TIMESTAMP, NULL, NULL, 0,
        CURRENT_TIMESTAMP, 'MANUAL', 'f027e5f9-31a1-42ac-90b3-dc437585b839');

REFRESH MATERIALIZED VIEW public.mv_player_match_facts;


