-- Older Hibernate-generated schemas stored java.time.Duration as NUMERIC seconds.
-- Convert those values to the PostgreSQL interval type expected by Event.duration.
-- Databases created by V1 already use INTERVAL and pass through unchanged.
DO $$
DECLARE
    duration_data_type TEXT;
BEGIN
    SELECT data_type
      INTO duration_data_type
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'event'
       AND column_name = 'duration';

    IF duration_data_type = 'interval' THEN
        RETURN;
    END IF;

    IF duration_data_type IS NULL THEN
        RAISE EXCEPTION 'event.duration column is missing; cannot normalize its type';
    END IF;

    IF duration_data_type NOT IN (
        'numeric', 'decimal', 'real', 'double precision', 'smallint', 'integer', 'bigint'
    ) THEN
        RAISE EXCEPTION 'Unsupported event.duration type: %', duration_data_type;
    END IF;

    EXECUTE $migration$
        ALTER TABLE event
        ALTER COLUMN duration TYPE INTERVAL SECOND(6)
        USING CASE
            WHEN duration IS NULL THEN NULL
            ELSE make_interval(secs => duration::double precision)
        END
    $migration$;
END $$;
