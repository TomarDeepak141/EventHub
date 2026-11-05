import { forwardRef, type InputHTMLAttributes } from 'react';

interface Props extends InputHTMLAttributes<HTMLInputElement> { label: string; error?: string; hint?: string; }
export const TextField = forwardRef<HTMLInputElement, Props>(function TextField({ label, error, hint, id, className, ...props }, ref) {
  const fieldId = id || props.name;
  return <div className="field">
    <label className="field-label" htmlFor={fieldId}>{label}</label>
    <input ref={ref} id={fieldId} aria-invalid={Boolean(error)} aria-describedby={error ? `${fieldId}-error` : hint ? `${fieldId}-hint` : undefined}
      className={`input ${error?'input-error':''} ${className||''}`} {...props} />
    {error && <p className="field-error" id={`${fieldId}-error`}>{error}</p>}
    {!error && hint && <p className="field-hint" id={`${fieldId}-hint`}>{hint}</p>}
  </div>;
});
