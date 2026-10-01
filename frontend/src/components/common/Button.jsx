import React from 'react';

export const Button = ({
  children,
  variant = 'primary', // primary | secondary | outline | danger | success | ghost | text
  size = 'md', // sm | md | lg
  type = 'button',
  disabled = false,
  loading = false,
  onClick,
  className = '',
  icon: Icon,
  iconPosition = 'left',
  ...props
}) => {
  const baseStyles =
    'relative inline-flex items-center justify-center font-medium rounded-lg transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100 shadow-sm';

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-4 py-2 text-sm gap-2',
    lg: 'px-5 py-2.5 text-base gap-2.5',
  };

  // High-contrast, theme-reactive button styles
  const variantStyles = {
    primary:
      'bg-theme-primary text-white hover:brightness-90 focus:ring-theme border border-transparent shadow',
    secondary:
      'bg-theme-surface text-theme-primary hover:bg-theme-elevated border border-theme focus:ring-slate-400',
    outline:
      'bg-transparent text-theme-brand hover:bg-theme-primary-light border-2 border-theme-primary focus:ring-theme font-semibold',
    danger:
      'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 border border-transparent shadow',
    success:
      'bg-emerald-600 text-white hover:bg-emerald-700 focus:ring-emerald-500 border border-transparent shadow',
    ghost:
      'bg-transparent text-theme-secondary hover:text-theme-primary hover:bg-black/5 dark:hover:bg-white/10 shadow-none focus:ring-slate-400',
    text:
      'bg-transparent text-theme-secondary hover:text-theme-brand shadow-none p-0 focus:ring-0',
  };

  const currentVariant = variantStyles[variant] || variantStyles.primary;

  return (
    <button
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={`${baseStyles} ${sizeStyles[size] || sizeStyles.md} ${currentVariant} ${className}`}
      {...props}
    >
      {loading ? (
        <svg
          className="animate-spin h-4 w-4 shrink-0"
          viewBox="0 0 24 24"
          fill="none"
          aria-hidden="true"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          ></circle>
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      ) : Icon && iconPosition === 'left' ? (
        <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />
      ) : null}

      <span>{children}</span>

      {!loading && Icon && iconPosition === 'right' ? (
        <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />
      ) : null}
    </button>
  );
};

export const IconButton = ({
  icon: Icon,
  label,
  tooltip,
  variant = 'ghost', // ghost | secondary | primary | danger
  size = 'md', // sm | md | lg
  onClick,
  disabled = false,
  className = '',
  ...props
}) => {
  const sizeStyles = {
    sm: 'p-1.5 text-xs',
    md: 'p-2 text-sm',
    lg: 'p-2.5 text-base',
  };

  const iconSizes = {
    sm: 'w-3.5 h-3.5',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  };

  const variantStyles = {
    ghost:
      'bg-transparent text-theme-secondary hover:text-theme-primary hover:bg-black/5 dark:hover:bg-white/10',
    secondary:
      'bg-theme-surface text-theme-secondary hover:text-theme-primary border border-theme shadow-sm',
    primary:
      'bg-theme-primary text-white hover:brightness-90 shadow-sm',
    danger:
      'bg-red-50 text-red-600 hover:bg-red-100 dark:bg-red-950/40 dark:text-red-400 dark:hover:bg-red-900/50',
    success:
      'bg-emerald-50 text-emerald-600 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:text-emerald-400',
  };

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      aria-label={label || tooltip}
      title={tooltip || label}
      className={`inline-flex items-center justify-center rounded-lg transition-all focus:outline-none focus:ring-2 focus:ring-theme disabled:opacity-40 disabled:cursor-not-allowed ${sizeStyles[size] || sizeStyles.md} ${variantStyles[variant] || variantStyles.ghost} ${className}`}
      {...props}
    >
      {Icon && <Icon className={iconSizes[size] || iconSizes.md} aria-hidden="true" />}
    </button>
  );
};

export default Button;
