CREATE TABLE squash_points
(
    id              UUID PRIMARY KEY,
    squash_match_id UUID        NOT NULL,
    point_order     INTEGER     NOT NULL,
    created_at      TIMESTAMP,
    server          VARCHAR(16) NOT NULL,
    service_side    VARCHAR(16) NOT NULL,
    scorer          VARCHAR(16) NOT NULL,
    score_a         INTEGER     NOT NULL,
    score_b         INTEGER     NOT NULL,
    CONSTRAINT fk_squash_points_match
        FOREIGN KEY (squash_match_id) REFERENCES squash_matches (id) ON DELETE CASCADE,
    CONSTRAINT uq_squash_points_match_order
        UNIQUE (squash_match_id, point_order)
);

CREATE INDEX idx_squash_points_match_id ON squash_points (squash_match_id);

INSERT INTO squash_points (
    id,
    squash_match_id,
    point_order,
    created_at,
    server,
    service_side,
    scorer,
    score_a,
    score_b
)
SELECT
    gen_random_uuid(),
    m.id,
    (p.ordinality - 1)::INTEGER,
    m.start_time,
    p.point->>'server',
    p.point->>'serviceSide',
    p.point->>'scorer',
    (p.point->>'scoreA')::INTEGER,
    (p.point->>'scoreB')::INTEGER
FROM squash_matches m
         CROSS JOIN LATERAL jsonb_array_elements(
        CASE
            WHEN m.points_history IS NULL
                OR btrim(m.points_history) = ''
                OR lower(btrim(m.points_history)) = 'null'
                THEN '[]'::jsonb
            ELSE m.points_history::jsonb
            END
                            ) WITH ORDINALITY AS p(point, ordinality);

ALTER TABLE squash_matches
    DROP COLUMN points_history;
