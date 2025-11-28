import React from 'react';

interface StepIndicatorProps {
    currentStep: number;
    steps: string[];
}

export const StepIndicator: React.FC<StepIndicatorProps> = ({ currentStep, steps }) => {
    return (
        <div className="flex items-center gap-4 border-b border-separator pb-6 mb-6">
            {steps.map((step, index) => {
                const stepNumber = index + 1;
                const isActive = stepNumber === currentStep;
                const isCompleted = stepNumber < currentStep;

                return (
                    <React.Fragment key={stepNumber}>
                        <div className="flex items-center gap-2">
                            <div
                                className={`flex size-6 items-center justify-center rounded-full text-sm font-bold transition-colors ${isActive
                                        ? 'bg-primary text-white'
                                        : isCompleted
                                            ? 'bg-green-500 text-white'
                                            : 'border border-gray-300 bg-white text-gray-500'
                                    }`}
                            >
                                {isCompleted ? (
                                    <span className="material-symbols-outlined text-base">check</span>
                                ) : (
                                    stepNumber
                                )}
                            </div>
                            <p
                                className={`text-sm font-semibold transition-colors ${isActive
                                        ? 'text-primary'
                                        : isCompleted
                                            ? 'text-green-600'
                                            : 'text-gray-500'
                                    }`}
                            >
                                {step}
                            </p>
                        </div>
                        {index < steps.length - 1 && (
                            <div className="h-px flex-1 bg-gray-200"></div>
                        )}
                    </React.Fragment>
                );
            })}
        </div>
    );
};
