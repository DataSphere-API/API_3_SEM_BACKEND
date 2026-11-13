-- US003-1: um código não pode identificar dois atendimentos diferentes.
CREATE UNIQUE INDEX uq_guia_proc_qr ON guia_proc (qr)
    WHERE qr IS NOT NULL AND TRIM(qr) <> '';
