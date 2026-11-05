import { forwardRef, type ButtonHTMLAttributes } from 'react';
import { LoaderCircle } from 'lucide-react';

type Props = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'ghost' | 'light'; loading?: boolean };
export const Button = forwardRef<HTMLButtonElement, Props>(function Button({ className, variant='primary', loading, children, disabled, ...props }, ref) {
  return <button ref={ref} className={`button button-${variant} ${className||''}`} disabled={disabled || loading} {...props}>
    {loading && <LoaderCircle size={16} className="animate-spin" />}{children}
  </button>;
});
