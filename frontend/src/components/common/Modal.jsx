import React, { useEffect } from 'react';
import { FiX } from 'react-icons/fi';

export const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  maxWidth = 'max-w-xl',
  footer,
}) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="min-h-screen px-4 text-center">
        {/* Backdrop */}
        <div
          className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm transition-opacity"
          onClick={onClose}
        />

        {/* Center alignment trick */}
        <span className="inline-block h-screen align-middle" aria-hidden="true">
          &#8203;
        </span>

        {/* Modal dialog */}
        <div className={`inline-block w-full ${maxWidth} p-6 my-8 text-left align-middle transition-all transform bg-white shadow-xl rounded-2xl border border-slate-100 relative z-10`}>
          <div className="flex items-center justify-between pb-3 border-b border-slate-200">
            <h3 className="text-lg font-bold text-slate-800 tracking-tight">
              {title}
            </h3>
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-slate-600 rounded-lg p-1 transition-colors focus:outline-none"
              aria-label="Close dialog"
            >
              <FiX className="w-5 h-5" />
            </button>
          </div>

          <div className="mt-4">{children}</div>

          {footer && (
            <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
              {footer}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Modal;
