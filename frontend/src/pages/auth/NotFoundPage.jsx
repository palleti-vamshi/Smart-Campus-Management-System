import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/common/Button';
import { FiAlertCircle, FiArrowLeft } from 'react-icons/fi';

export const NotFoundPage = () => {
  const { getDefaultRoute } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-slate-200 p-8 text-center">
        <div className="w-16 h-16 bg-slate-100 text-slate-500 rounded-full flex items-center justify-center mx-auto mb-4">
          <FiAlertCircle className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold text-slate-800">404 - Page Not Found</h1>
        <p className="mt-2 text-sm text-slate-600">
          The page or academic resource you are looking for does not exist or has been relocated.
        </p>
        <div className="mt-6 flex justify-center">
          <Button
            variant="primary"
            onClick={() => navigate(getDefaultRoute())}
            icon={FiArrowLeft}
          >
            Return to Dashboard
          </Button>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
